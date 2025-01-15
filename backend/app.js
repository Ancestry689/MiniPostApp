const express = require('express');
const bodyParser = require('body-parser');
const multer = require('multer');
const fs = require('fs');
const cors = require('cors'); // 引入 cors 模块
const mysql = require('mysql');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const path = require('path');
const crypto = require('crypto');


const app = express();
const PORT = 3000;

// 服务器地址
const SERVER_URL = 'http://192.168.149.132:3000'; // 服务器地址

// 引入数据库连接
const db = require('./db');

// 配置 CORS
app.use(cors()); // 启用 CORS 支持

// 配置 body-parser 中间件
app.use(bodyParser.json());

// JWT Secret Key
const JWT_SECRET = crypto.randomBytes(32).toString('hex'); // 生成 64 字符的随机字符串
console.log('JWT_SECRET:', JWT_SECRET);


function generateToken(user) {
    return jwt.sign({ id: user.id, username: user.username }, JWT_SECRET, { expiresIn: '1h' });
}


function verifyToken(req, res, next) {
    const token = req.headers['authorization'];
    if (!token) {
        return res.status(401).json({ message: 'No token provided' });
    }
    jwt.verify(token, JWT_SECRET, (err, decoded) => {
        if (err) {
            return res.status(401).json({ message: 'Invalid token' });
        }
        req.user = decoded;
        next();
    });
}

// 基本路由
app.get('/',(req, res) => res.send('hello!'))

// 设置文件存储路径和文件名
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        const uploadDir = path.join(__dirname, 'uploads');
        if (!fs.existsSync(uploadDir)) {
            fs.mkdirSync(uploadDir); // 如果 uploads 目录不存在，则创建
        }
        cb(null, uploadDir); // 文件存储路径
    },
    filename: (req, file, cb) => {
        const userId = req.body.userId; // 从请求中获取用户 ID

        const ext = path.extname(file.originalname); // 获取文件扩展名
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1e9); // 生成唯一后缀
        cb(null, `${userId}-${uniqueSuffix}${ext}`); // 使用用户 ID 和时间戳作为文件名

    }
});

// 初始化 multer
const upload = multer({ storage });


// 用户注册
app.post('/register', (req, res) => {
    const { username, password, email } = req.body;

    // 检查用户名是否已存在
    const checkUserQuery = 'SELECT * FROM users WHERE username = ?';
    db.query(checkUserQuery, [username], (err, results) => {
        if (err) return res.status(500).json({ message: 'Database error' });
        if (results.length > 0) return res.status(400).json({ message: 'Username already exists' });

        // 加密密码
        bcrypt.hash(password, 10, (err, hashedPassword) => {
            if (err) return res.status(500).json({ message: 'Password hashing error' });

            // 插入新用户
            const insertUserQuery = "INSERT INTO users (username, password, email, created_at) VALUES (?, ?, ?, CONVERT_TZ(NOW(), '+00:00', '+08:00'))";

            db.query(insertUserQuery, [username, hashedPassword, email], (err, result) => {
                if (err) return res.status(500).json({ message: 'Database error' });
                
                                // 打印 result 对象，检查插入操作的结果
                console.log('Insert result:', result);
                res.status(201).json({ message: 'User registered successfully' });
            });
        });
    });
});

// 用户登录
app.post('/login', (req, res) => {
    const { username, password } = req.body;

    // 查找用户
    const findUserQuery = 'SELECT * FROM users WHERE username = ?';
    db.query(findUserQuery, [username], (err, results) => {
        if (err) return res.status(500).json({ message: 'Database error' });
        if (results.length === 0) return res.status(400).json({ message: 'User not found' });

        const user = results[0];

        // 验证密码
        bcrypt.compare(password, user.password, (err, isMatch) => {
            if (err) return res.status(500).json({ message: 'Password comparison error' });
            if (!isMatch) return res.status(400).json({ message: 'Invalid password' });

            // 生成 JWT 令牌
            const token = generateToken(user);
            console.log(user);
            // 返回登录成功信息，包括 token、id 和 email
            res.status(200).json({
                message: 'Login successful',
                username: user.username,
                password: user.password,
                email: user.email, // 从 user 对象中获取 email
                id: user.id, // 从 user 对象中获取 id
                token: token
            });
        });
    });
});

// 静态文件服务（用于提供上传的图片）
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// 上传头像接口
app.post('/upload-avatar', upload.single('avatar'), (req, res) => {
    if (!req.file) {
        return res.status(400).json({ message: 'No file uploaded' });
    }

    const userId = req.body.userId; // 假设前端传递了 userId
    if (!userId) {
        return res.status(400).json({ message: 'User ID is required' });
    }

    const filePath = req.file.path; // 上传文件的临时路径
    const fileName = `${userId}_${Date.now()}${path.extname(req.file.originalname)}`; // 生成唯一文件名
    const newFilePath = path.join('uploads/avatars', fileName);

    // 查询用户的旧头像路径
    const getOldAvatarSql = 'SELECT avatar_path FROM users WHERE id = ?';
    db.query(getOldAvatarSql, [userId], (err, results) => {
        if (err) {
            return res.status(500).json({ message: 'Database error: Failed to fetch old avatar' });
        }

        const oldAvatarPath = results[0]?.avatar_path; // 获取旧头像路径

        // 将文件移动到永久存储路径
        fs.rename(filePath, newFilePath, (err) => {
            if (err) {
                return res.status(500).json({ message: 'Failed to save file' });
            }

            // 更新数据库中的 avatar_path
            const updateAvatarSql = 'UPDATE users SET avatar_path = ? WHERE id = ?';
            db.query(updateAvatarSql, [newFilePath, userId], (err, result) => {
                if (err) {
                    return res.status(500).json({ message: 'Database error: Failed to update avatar' });
                }

                // 删除旧头像文件
                if (oldAvatarPath) {
                    fs.unlink(oldAvatarPath, (err) => {
                        if (err) {
                            console.error('Failed to delete old avatar:', err);
                        } else {
                            console.log('Old avatar deleted successfully');
                        }
                    });
                }

                res.status(200).json({ message: 'Avatar uploaded successfully', avatarPath: newFilePath });
            });
        });
    });
});


// 获取用户头像接口
app.get('/avatar/:userId', (req, res) => {
    const userId = req.params.userId;

    // 查询用户的 avatar_path
    const sql = 'SELECT avatar_path FROM users WHERE id = ?';
    db.query(sql, [userId], (err, result) => {
        if (err) {
            return res.status(500).json({ message: 'Database error' });
        }
        if (result.length === 0 || !result[0].avatar_path) {
            return res.status(404).json({ message: 'Avatar not found' });
        }

        const avatarPath = result[0].avatar_path;

        res.sendFile(path.resolve(avatarPath)); // 返回头像文件
    });
});

app.post('/update-user-info', (req, res) => {
    const { userId, username, password, email } = req.body;


    // 检查新用户名是否已存在
    const checkUsernameQuery = 'SELECT * FROM users WHERE username = ? AND id != ?';
    db.query(checkUsernameQuery, [username, userId], (err, results) => {
        if (err) {
            return res.status(500).json({ message: '数据库错误', error: err });
        }
        if (results.length > 0) {
            return res.status(400).json({ message: '用户名已存在' });
        }

        // 加密密码
        bcrypt.hash(password, 10, (err, hashedPassword) => {
            if (err) {
                return res.status(500).json({ message: '密码加密失败', error: err });
            }

            // 更新用户信息
            const updateUserQuery = `
                UPDATE users
                SET username = ?, password = ?, email = ?
                WHERE id = ?
            `;
            db.query(updateUserQuery, [username, hashedPassword, email, userId], (err, result) => {
                if (err) {
                    return res.status(500).json({ message: '数据库错误', error: err });
                }
                res.status(200).json({ message: '用户信息更新成功' });
            });
        });
    });
});

// 创建帖子 API
app.post('/posts', upload.array('images'), (req, res) => {
    const { title, content, author_id } = req.body;
    const images = req.files;

    if (!title || !content || !author_id) {
        return res.status(400).json({ message: '缺少必要参数' });
    }

 // 插入帖子数据到 posts 表
    const postSql = "INSERT INTO posts (title, content, author_id, created_at) VALUES (?, ?, ?, CONVERT_TZ(NOW(), '+00:00', '+08:00'))";
    db.query(postSql, [title, content, author_id], (err, result) => {
        if (err) {
            return res.status(500).json({ message: 'Database error: Failed to insert post' });
        }

        const postId = result.insertId; // 获取插入的帖子 ID

        // 处理上传的图片
        if (req.files && req.files.length > 0) {
            const imagePromises = req.files.map(file => {

            
                const filePath = file.path; // 上传文件的临时路径
                const fileName = `${postId}_${Date.now()}_${Math.floor(Math.random() * 10000)}${path.extname(file.originalname)}`; // 生成唯一文件名
                const newFilePath = path.join('uploads/posts', fileName);

                // 将文件移动到永久存储路径
                return new Promise((resolve, reject) => {
                                    // 检查临时文件是否存在
                    if (!fs.existsSync(filePath)) {
                        console.error('Temporary file not found:', filePath);
                        reject('Temporary file not found');
                        return;
                    }
                
                    fs.rename(filePath, newFilePath, (err) => {
                        if (err) {
                            console.error('File move error:', err); // 打印文件移动错误
                            reject('Failed to save file');
                        } else {
                            // 插入图片数据到 post_images 表
                            const imageSql = 'INSERT INTO post_images (post_id, image_path) VALUES (?, ?)';
                            db.query(imageSql, [postId, newFilePath], (err, result) => {
                                if (err) {
                                    reject('Database error: Failed to insert image');
                                } else {
                                    resolve();
                                }
                            });
                        }
                    });
                });
            });

            // 等待所有图片处理完成
            Promise.all(imagePromises)
                .then(() => {
                    res.status(200).json({ message: 'Post created successfully', postId });
                })
                .catch(error => {
                    console.error('Promise.all error:', error); // 打印 Promise.all 的错误
                    res.status(500).json({ message: error });
                });
  
        } else {
            res.status(200).json({ message: 'Post created successfully (no images)', postId });
        }
    });
});

// 将图片路径转换为 URL
const convertImagePathToUrl = (imagePath) => {
    if (!imagePath) return null;
    return `${SERVER_URL}/${imagePath}`; // 使用 SERVER_URL 变量
};

// 获取帖子列表
app.get('/posts', (req, res) => {
    const page = parseInt(req.query.page) || 1; // 当前页码
    const limit = parseInt(req.query.limit) || 20; // 每页显示的帖子数量
    const offset = (page - 1) * limit;

    // 查询帖子数据
    const sql = `
        SELECT p.id, p.title, p.content, p.author_id, p.created_at, 
               GROUP_CONCAT(pi.image_path) AS images
        FROM posts p
        LEFT JOIN post_images pi ON p.id = pi.post_id
        GROUP BY p.id
        LIMIT ? OFFSET ?
    `;

    db.query(sql, [limit, offset], (err, results) => {
        if (err) {
            console.error('Database error:', err);
            return res.status(500).json({ message: 'Database error' });
        }

        // 格式化数据
        const posts = results.map(row => ({
            postId: row.id,
            title: row.title,
            content: row.content,
            authorId: row.author_id,
            createdAt: row.created_at,
            image_url: row.images ? row.images.split(',').map(convertImagePathToUrl) : []
        }));
        
        res.status(200).json(posts);
    });
});

// 获取图片文件
app.get('/uploads/:filename', (req, res) => {
    const filename = req.params.filename;
    const filePath = path.join(__dirname, 'uploads', filename);

    // 检查文件是否存在
    if (!fs.existsSync(filePath)) {
        return res.status(404).json({ message: 'File not found' });
    }

    // 返回图片文件
    res.sendFile(filePath);
});

// 获取帖子详情
app.get('/posts/:postId', (req, res) => {
    const postId = req.params.postId;

    // 查询帖子数据和作者信息
    const sql = `
        SELECT p.id, p.title, p.content, p.author_id, p.created_at, p.like_count,
               u.username AS author_name, u.avatar_path AS author_avatar,
               GROUP_CONCAT(pi.image_path) AS images
        FROM posts p
        JOIN users u ON p.author_id = u.id
        LEFT JOIN post_images pi ON p.id = pi.post_id
        WHERE p.id = ?
        GROUP BY p.id
    `;
    
    db.query(sql, [postId], (err, postResults) => {
        if (err) {
            console.error('Database error:', err);
            return res.status(500).json({ message: 'Database error' });
        }

        if (postResults.length === 0) {
            return res.status(404).json({ message: 'Post not found' });
        }

        const post = {
            postId: postResults[0].id,
            title: postResults[0].title,
            content: postResults[0].content,
            authorId: postResults[0].author_id,
            createdAt: postResults[0].created_at,
            like_count: postResults[0].like_count,
            author_name: postResults[0].author_name,
            author_avatar: convertImagePathToUrl(postResults[0].author_avatar),
            image_url: postResults[0].images ? postResults[0].images.split(',').map(convertImagePathToUrl) : []
        };
        
        console.log(post);

        // 查询评论数据
        const commentSql = `
            SELECT c.id, c.content, c.author_id, c.created_at
            FROM comments c
            WHERE c.post_id = ?
            ORDER BY c.created_at DESC
            LIMIT 10
        `;

        db.query(commentSql, [postId], (err, commentResults) => {
            if (err) {
                console.error('Database error:', err);
                return res.status(500).json({ message: 'Database error' });
            }

            post.comments = commentResults;
            res.status(200).json(post);
        });
    });
});

// 点赞帖子
app.post('/posts/:postId/like', (req, res) => {
    const postId = req.params.postId;
    const sql = 'UPDATE posts SET like_count = like_count + 1 WHERE id = ?';
    db.query(sql, [postId], (err, result) => {
        if (err) {
            return res.status(500).json({ message: 'Database error' });
        }
        if (result.affectedRows === 0) {
            return res.status(404).json({ message: 'Post not found' });
        }
        // 获取更新后的点赞数
        const getLikeCountSql = 'SELECT like_count FROM posts WHERE id = ?';
        db.query(getLikeCountSql, [postId], (err, result) => {
            if (err) {
                return res.status(500).json({ message: 'Database error' });
            }
            const likeCount = result[0].like_count;
            res.status(200).json({ message: 'Post liked successfully', likeCount: likeCount });
        });
    });
});

// 发布评论
app.post('/posts/:postId/comments', (req, res) => {
    const postId = req.params.postId;
    const { authorId, content } = req.body;

    if (!authorId || !content) {
        return res.status(400).json({ message: 'Author ID and content are required' });
    }

    const sql = "INSERT INTO comments (post_id, author_id, content, created_at) VALUES (?, ?, ?,CONVERT_TZ(NOW(), '+00:00', '+08:00'))";
    db.query(sql, [postId, authorId, content], (err, result) => {
        if (err) {
            console.error('Database error:', err);
            return res.status(500).json({ message: 'Database error' });
        }

        // 返回新创建的评论
        const newCommentId = result.insertId;
        const selectSql = `
            SELECT comments.id, comments.content, comments.created_at,
                   users.id AS author_id, users.username AS author_name, users.avatar_path AS author_avatar
            FROM comments
            JOIN users ON comments.author_id = users.id
            WHERE comments.id = ?
        `;
        
        db.query(selectSql, [newCommentId], (err, results) => {
            if (err) {
                console.error('Database error:', err);
                return res.status(500).json({ message: 'Database error' });
            }
            
            const comment = {
                id: results[0].id,
                content: results[0].content,
                createdAt: results[0].created_at,
                authorId: results[0].author_id,
                authorName: results[0].author_name,
                authorAvatar: convertImagePathToUrl(results[0].author_avatar),
            };
            console.log(comment);
            
            res.status(201).json(comment);
        });
    });
});

// 获取评论列表
app.get('/posts/:postId/comments', (req, res) => {
    const postId = req.params.postId;
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const offset = (page - 1) * limit;

    const query = `
        SELECT comments.*, users.username AS authorName, users.avatar_path AS authorAvatarUrl
        FROM comments
        JOIN users ON comments.author_id = users.id
        WHERE comments.post_id = ?
        ORDER BY comments.created_at DESC
        LIMIT ? OFFSET ?
    `;

    db.query(query, [postId, parseInt(limit), parseInt(offset)], (err, results) => {
        if (err) {
            console.log("error");
            return res.status(500).json({ message: 'Database error' });
        }
        
            const comments = results.map(row => ({
              id: row.id,
              content: row.content,
              authorId: row.author_id,
              authorName: row.authorName,
              authorAvatar: convertImagePathToUrl(row.authorAvatarUrl),
              createdAt: row.created_at
            }));
        console.log(comments);

        res.status(200).json(comments);
    });
});

// 启动服务器
app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});