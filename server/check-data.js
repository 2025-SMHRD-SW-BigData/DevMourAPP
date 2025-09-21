const mysql = require('mysql2/promise');

const dbConfig = {
    host: 'project-db-campus.smhrd.com',
    port: 3307,
    user: 'campus_25SW_BD_p3_2',
    password: 'smhrd2',
    database: 'campus_25SW_BD_p3_2'
};

async function checkData() {
    const connection = await mysql.createConnection(dbConfig);
    
    try {
        console.log('데이터베이스 연결 성공!');
        
        // t_markers 테이블의 모든 데이터 조회
        const [rows] = await connection.execute('SELECT * FROM t_markers');
        
        console.log(`\n총 ${rows.length}개의 마커 데이터가 있습니다:`);
        console.log('='.repeat(80));
        
        rows.forEach((row, index) => {
            console.log(`${index + 1}. 마커 ID: ${row.marker_id}`);
            console.log(`   위도(lat): ${row.lat}`);
            console.log(`   경도(lon): ${row.lon}`);
            console.log(`   마커 타입: ${row.marker_type}`);
            console.log(`   제어 인덱스: ${row.control_idx}`);
            console.log(`   CCTV 인덱스: ${row.cctv_idx}`);
            console.log('-'.repeat(40));
        });
        
        if (rows.length === 0) {
            console.log('t_markers 테이블에 데이터가 없습니다.');
        }
        
    } catch (error) {
        console.error('데이터 조회 실패:', error);
    } finally {
        await connection.end();
    }
}

// 스크립트 실행
checkData();
