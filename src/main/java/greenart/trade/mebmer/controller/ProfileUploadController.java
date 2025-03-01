package greenart.trade.mebmer.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class ProfileUploadController {

    // 업로드할 디렉토리 경로 (application.properties에 정의된 값을 주입받습니다)
    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    // 정적 리소스 경로 (URL Prefix)
    @Value("${file.url-prefix:http://localhost:8080/uploads}")
    private String urlPrefix;


    // 이미지 업로드 처리
    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        // 파일이 비어있는지 확인
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "파일이 비어 있습니다."));
        }

        try {
            // 업로드할 디렉토리가 없으면 생성
            Path path = Paths.get(uploadDir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);  // 디렉토리 생성
            }

            // 고유한 파일명 생성 (중복 방지)
            String originalFilename = file.getOriginalFilename();
            String filename = UUID.randomUUID() + "_" + originalFilename;

            // 저장 경로 설정
            Path filePath = path.resolve(filename);

            // 파일 저장
            file.transferTo(filePath.toFile());

            // 업로드된 파일의 URL 생성
            String fileUrl = urlPrefix + "/" + filename;

            // 업로드 성공 시 파일 URL 반환
            String imageUrl = "/uploads/" + filename;
            return ResponseEntity.ok(Map.of("url", imageUrl));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }


}