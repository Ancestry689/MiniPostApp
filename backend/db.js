// db.js
const mysql = require('mysql');

const db = mysql.createConnection({
    host: 'localhost', // 数据库主机地址
    user: 'root',      // 数据库用户名
    password: '12345678', // 数据库密码
    database: 'minipost_database' // 数据库名称
});

db.connect((err) => {
    if (err) {
        console.error('Database connection failed: ', err.stack);
        return;
    }
    console.log('Connected to MySQL database');
});

module.exports = db;