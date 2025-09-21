# AWS S3 설정 가이드

## 1. AWS S3 버킷 생성

1. AWS 콘솔에 로그인
2. S3 서비스로 이동
3. "버킷 만들기" 클릭
4. 버킷 이름: `devmour` (또는 원하는 이름)
5. 리전: `아시아 태평양 (서울) ap-southeast-2`
6. 퍼블릭 액세스 차단 설정을 해제 (이미지 공개 접근을 위해)

## 2. IAM 사용자 생성 및 권한 설정

1. IAM 서비스로 이동
2. "사용자" → "사용자 추가"
3. 사용자 이름: `devmour`
4. 프로그래밍 방식 액세스 선택
5. 권한 정책 연결: `AmazonS3FullAccess` 또는 다음 커스텀 정책:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:PutObjectAcl",
                "s3:GetObject",
                "s3:DeleteObject"
            ],
            "Resource": "arn:aws:s3:::devmour/*"
        }
    ]
}
```

## 3. 환경 변수 설정

`server/config/s3Config.js` 파일에서 다음 값들을 실제 AWS 자격 증명으로 변경:

```javascript
const s3Config = {
    accessKeyId: 'AKIASKD5PB3ZHMNVFSVG',
    secretAccessKey: '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',
    region: 'ap-southeast-2',
    bucketName: 'devmour'
};
```

## 4. 패키지 설치

```bash
cd server
npm install aws-sdk
```

## 5. 서버 실행

```bash
npm start
```

## 6. 테스트

민원 제출 시 이미지가 S3에 업로드되고, 데이터베이스에는 S3 URL이 저장됩니다.

## 주의사항

- AWS 자격 증명은 절대 공개 저장소에 커밋하지 마세요
- 프로덕션 환경에서는 환경 변수를 사용하세요
- S3 버킷의 CORS 설정이 필요할 수 있습니다
