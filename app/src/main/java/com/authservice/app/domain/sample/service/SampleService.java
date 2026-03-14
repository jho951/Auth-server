package com.authservice.app.domain.sample.service;

import com.authservice.app.domain.sample.dto.SampleRequest;
import com.authservice.app.domain.sample.dto.SampleResponse;

public interface SampleService {
	SampleResponse.SampleCreateResponse create(SampleRequest.SampleCreateRequest dto);

	SampleResponse.SampleUpdateResponse update(SampleRequest.SampleUpdateRequest dto);

	SampleResponse.SampleReadListResponse read(SampleRequest.SampleReadRequest dto);

	SampleResponse.SampleReadResponse readDetail(Long id);

	SampleResponse.SampleDeleteResponse delete(SampleRequest.SampleDeleteRequest dto);
}
