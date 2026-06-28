ackage com.wanted.backend.domain.community.presentation;

import com.wanted.backend.domain.community.application.command.FileUploadCommand;
import com.wanted.backend.domain.community.application.usecase.FileUploadUseCase;
import com.wanted.backend.domain.community.presentation.response.FileUploadResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Tag(name = "File Upload", description = "커뮤니티 파일 업로드 API")
@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileUploadUseCase fileUploadUseCase;

    public FileUploadController(FileUploadUseCase fileUploadUseCase) {
        this.fileUploadUseCase = fileUploadUseCase;
    }


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "파일 업로드",
            description = """
                파일을 서버 로컬 디렉토리에 저장하고 접근 URL을 반환합니다.
                - 로그인한 회원만 업로드할 수 있습니다.
                - 허용 확장자: jpg, jpeg, png
                - fileType으로 업로드 목적을 구분합니다. (예: post, comment 등)
                - 요청 타입은 multipart/form-data 입니다.
                """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "파일 업로드 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "허용되지 않는 파일 형식"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "업로드 목적 구분 (예: post, comment)", example = "post")
            @RequestParam("fileType") String fileType,
            @RequestPart("file") MultipartFile file) {

        FileUploadResponse response = fileUploadUseCase.handle(
                new FileUploadCommand(userDetails.getMemberId(), file, fileType));

        return ApiResponse.created("파일이 업로드되었습니다.", response);
    }
}