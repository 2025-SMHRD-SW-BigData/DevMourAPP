const AWS = require('aws-sdk');

// AWS S3 설정 - 환경 변수 필수
const s3Config = {
    accessKeyId: process.env.AWS_ACCESS_KEY_ID,
    secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY,
    region: process.env.AWS_REGION || 'ap-southeast-2',
    bucketName: process.env.AWS_S3_BUCKET_NAME || 'devmour'
};

// 필수 환경 변수 검증
if (!s3Config.accessKeyId || !s3Config.secretAccessKey) {
    throw new Error('AWS_ACCESS_KEY_ID와 AWS_SECRET_ACCESS_KEY 환경 변수가 필요합니다.');
}

// S3 인스턴스 생성
const s3 = new AWS.S3({
    accessKeyId: s3Config.accessKeyId,
    secretAccessKey: s3Config.secretAccessKey,
    region: s3Config.region
});

// S3에 파일 업로드 함수
const uploadToS3 = async (file, folder = 'reports') => {
    try {
        const fileExtension = file.originalname.split('.').pop();
        const fileName = `${folder}/${Date.now()}_${Math.round(Math.random() * 1E9)}.${fileExtension}`;
        
        const uploadParams = {
            Bucket: s3Config.bucketName,
            Key: fileName,
            Body: file.buffer,
            ContentType: file.mimetype,
            ACL: 'public-read' // 공개 읽기 권한
        };

        const result = await s3.upload(uploadParams).promise();
        
        return {
            success: true,
            url: result.Location,
            key: fileName
        };
    } catch (error) {
        console.error('S3 업로드 오류:', error);
        return {
            success: false,
            error: error.message
        };
    }
};

// S3에서 파일 삭제 함수
const deleteFromS3 = async (key) => {
    try {
        const deleteParams = {
            Bucket: s3Config.bucketName,
            Key: key
        };

        await s3.deleteObject(deleteParams).promise();
        return { success: true };
    } catch (error) {
        console.error('S3 삭제 오류:', error);
        return { success: false, error: error.message };
    }
};

module.exports = {
    s3Config,
    uploadToS3,
    deleteFromS3
};
