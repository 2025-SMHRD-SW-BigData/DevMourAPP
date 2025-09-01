const express = require('express');
const router = express.Router();
const mysql = require('mysql2/promise');

// 데이터베이스 연결 설정
const dbConfig = {
    host: 'project-db-campus.smhrd.com',
    port: 3307,
    user: 'campus_25SW_BD_p3_2',
    password: 'smhrd2',
    database: 'campus_25SW_BD_p3_2'
};

// 도로 통제 목록 조회 - t_road_control 테이블에서 데이터 조회
router.get('/', async (req, res) => {
    try {
        // 데이터베이스 연결
        const connection = await mysql.createConnection(dbConfig);
        
        // t_road_control 테이블에서 모든 도로 통제 데이터 조회
        const [rows] = await connection.execute(`
            SELECT 
                control_idx,
                pred_idx,
                control_desc,
                control_st_tm,
                control_ed_tm,
                created_at,
                road_idx,
                lat,
                lon,
                control_addr,
                control_type,
                completed
            FROM t_road_control
            ORDER BY control_idx
        `);
        
        await connection.end();
        
        // 응답 데이터 형식에 맞게 변환
        const roadControls = rows.map(row => ({
            control_idx: row.control_idx,
            pred_idx: row.pred_idx,
            control_desc: row.control_desc,
            control_st_tm: row.control_st_tm,
            control_ed_tm: row.control_ed_tm,
            created_at: row.created_at,
            road_idx: row.road_idx,
            lat: parseFloat(row.lat),
            lon: parseFloat(row.lon),
            control_addr: row.control_addr,
            control_type: row.control_type,
            completed: row.completed
        }));
        
        res.json({
            success: true,
            message: '도로 통제 데이터 조회 성공',
            data: roadControls
        });
        
    } catch (error) {
        console.error('도로 통제 데이터 조회 오류:', error);
        res.status(500).json({
            success: false,
            message: '도로 통제 데이터 조회에 실패했습니다',
            error: error.message
        });
    }
});

module.exports = router;
