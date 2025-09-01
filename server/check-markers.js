const mysql = require('mysql2/promise');

const dbConfig = {
    host: 'project-db-campus.smhrd.com',
    port: 3307,
    user: 'campus_25SW_BD_p3_2',
    password: 'smhrd2',
    database: 'campus_25SW_BD_p3_2'
};

async function checkMarkers() {
    const connection = await mysql.createConnection(dbConfig);
    
    try {
        console.log('데이터베이스 연결 성공!\n');

        // 전체 마커 수 확인
        const [countResult] = await connection.execute('SELECT COUNT(*) as total FROM t_markers');
        console.log(`총 마커 수: ${countResult[0].total}개`);
        
        // 모든 마커 데이터 조회
        const [rows] = await connection.execute('SELECT * FROM t_markers ORDER BY marker_id');
        
        console.log('\n=== 모든 마커 데이터 ===');
        rows.forEach((marker, index) => {
            console.log(`${index + 1}. 마커 ID: ${marker.marker_id}`);
            console.log(`   위도(lat): ${marker.lat}`);
            console.log(`   경도(lon): ${marker.lon}`);
            console.log(`   마커 타입: ${marker.marker_type}`);
            console.log(`   제어 인덱스: ${marker.control_idx}`);
            console.log(`   CCTV 인덱스: ${marker.cctv_idx}`);
            console.log('----------------------------------------');
        });
        
        // flood와 construction 타입만 조회
        const [filteredMarkers] = await connection.execute(
            'SELECT * FROM t_markers WHERE marker_type IN ("flood", "construction") AND lat IS NOT NULL AND lon IS NOT NULL'
        );
        
        console.log(`\n=== flood/construction 타입 마커 (${filteredMarkers.length}개) ===`);
        filteredMarkers.forEach((marker, index) => {
            console.log(`${index + 1}. 마커 ID: ${marker.marker_id}`);
            console.log(`   위도(lat): ${marker.lat}`);
            console.log(`   경도(lon): ${marker.lon}`);
            console.log(`   마커 타입: ${marker.marker_type}`);
            console.log(`   제어 인덱스: ${marker.control_idx}`);
            console.log(`   CCTV 인덱스: ${marker.cctv_idx}`);
            console.log('----------------------------------------');
        });
        
    } catch (error) {
        console.error('데이터베이스 조회 실패:', error);
    } finally {
        await connection.end();
    }
}

// 스크립트 실행
checkMarkers();
