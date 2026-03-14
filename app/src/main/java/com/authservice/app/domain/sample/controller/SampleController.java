package com.authservice.app.domain.sample.controller;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import com.authservice.app.domain.sample.dto.SampleRequest;
import com.authservice.app.domain.sample.dto.SampleResponse;
import com.authservice.app.common.base.dto.GlobalResponse;
import com.authservice.app.common.swagger.constant.SwaggerTag;
import com.authservice.app.domain.sample.service.SampleService;
import com.authservice.app.common.base.constant.SuccessCode;

@RestController
@RequestMapping("api/v1/sample")
@RequiredArgsConstructor
@Tag(name = SwaggerTag.SAMPLE, description = "This is a sample controller")
public class SampleController {
    private final SampleService sampleService;

    /**
     * sample 생성
     *
     * @param dto createRequestDto
     * @return SuccessCode, response
     */
    @PostMapping("/create")
    @Operation(summary = "Create", description = "sample 생성")
    @ApiResponse(responseCode = "200", description = "성공적으로 생성하였습니다.")
    public GlobalResponse<SampleResponse.SampleCreateResponse> create(@RequestBody @Valid SampleRequest.SampleCreateRequest dto) {
        SampleResponse.SampleCreateResponse response = sampleService.create(dto);
        return GlobalResponse.ok(SuccessCode.SUCCESS, response);
    }
    /**
     * sample 수정
     *
     * @param dto updateRequestDto
     * @return SuccessCode, response
     */
    @PutMapping("/update")
    @Operation(summary = "Update", description = "sample 수정")
    @ApiResponse(responseCode = "200", description = "성공적으로 수정하였습니다.")
    public GlobalResponse<SampleResponse.SampleUpdateResponse> update(@RequestBody @Valid SampleRequest.SampleUpdateRequest dto) {
        SampleResponse.SampleUpdateResponse response = sampleService.update(dto);
        return GlobalResponse.ok(SuccessCode.SUCCESS,response);
    }
    /**
     * sample 조회
     *
     * @param dto readRequestDto
     * @return SuccessCode, response
     */
    @GetMapping("/read")
    @Operation(summary = "sample read api", description = "sample 출력")
    @ApiResponse(responseCode = "200", description = "성공적으로 가져왔습니다.")
    public GlobalResponse<SampleResponse.SampleReadListResponse> read(
        @ModelAttribute @Valid SampleRequest.SampleReadRequest dto) {
        SampleResponse.SampleReadListResponse response = sampleService.read(dto);
        return GlobalResponse.ok(SuccessCode.SUCCESS, response);
    }
    /**
     * sample 상세
     *
     * @param id sample id
     * @return SuccessCode, response
     */
    @GetMapping("/read/detail/{id}")
    @Operation(summary = "sample read by id api", description = "해당 id의 sample 출력")
    @ApiResponse(responseCode = "200", description = "성공적으로 가져왔습니다.")
    public GlobalResponse<SampleResponse.SampleReadResponse> readDetail(@PathVariable Long id) {
        SampleResponse.SampleReadResponse response = sampleService.readDetail(id);
        return GlobalResponse.ok(SuccessCode.SUCCESS, response);
    }
    /**
     * sample 삭제
     *
     * @param dto deleteRequestDto
     * @return SuccessCode, response
     */
    @DeleteMapping("/delete")
    @Operation(summary = "sample delete api", description = "sample 삭제")
    @ApiResponse(responseCode = "200", description = "성공적으로 삭제했습니다.")
    public GlobalResponse<SampleResponse.SampleDeleteResponse> delete(@RequestBody SampleRequest.SampleDeleteRequest dto) {
        SampleResponse.SampleDeleteResponse response = sampleService.delete(dto);
        return GlobalResponse.ok(SuccessCode.SUCCESS,response);
    }
}
