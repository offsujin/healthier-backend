package com.healthier.diagnosis.service;

import com.healthier.diagnosis.domain.diagnosis.Diagnosis;
import com.healthier.diagnosis.domain.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthier.diagnosis.domain.oauth.KakaoProfile;
import com.healthier.diagnosis.dto.SaveDiagnosisRequestDto;
import com.healthier.diagnosis.dto.SaveDiagnosisResponseDto;
import com.healthier.diagnosis.exception.CustomException;
import com.healthier.diagnosis.exception.ErrorCode;
import com.healthier.diagnosis.repository.DiagnosisRepository;
import com.healthier.diagnosis.repository.UserRepository;
import com.healthier.diagnosis.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider tokenProvider;
    ModelMapper modelMapper = new ModelMapper();


    @SneakyThrows
    public String getToken(String accessToken) {

        // 카카오 서버에서 사용자 정보 가져오기
        RestTemplate rt = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers);

        ResponseEntity<String> kakaoProfileResponse = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoProfileRequest,
                String.class
        );

        KakaoProfile kakaoProfile = objectMapper.readValue(kakaoProfileResponse.getBody(), KakaoProfile.class);

        // User 저장
        User user = userRepository.findByEmail(kakaoProfile.getKakao_account().getEmail())
                .orElseGet(() ->
                    userRepository.save(
                            User.builder()
                                    .nickname(kakaoProfile.getKakao_account().getProfile().getNickname())
                                    .email(kakaoProfile.getKakao_account().getEmail())
                                    .build()
                    )
                );

        return tokenProvider.createToken(user);
    }

    // 진단 기록장 목록
    public List<SaveDiagnosisResponseDto> getDiagnosisList(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return getList(user);
    }

    // 진단 결과 저장
    public List<SaveDiagnosisResponseDto> saveMyDiagnosis(String email, SaveDiagnosisRequestDto dto) {
        String id = dto.getDiagnosisId();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Diagnosis diagnosis = diagnosisRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.DIAGNOSIS_NOT_FOUND));

        User.Record record = User.Record.builder()
                .diagnosis_id(id)
                .title(diagnosis.getTitle())
                .severity(diagnosis.getSeverity())
                .is_created(LocalDateTime.now())
                .build();

        user.getRecords().add(record);

        return getList(user);
    }

    // 진단 기록장 DTO로 변환
    private List<SaveDiagnosisResponseDto> getList(User user) {
        return (List<SaveDiagnosisResponseDto>) SaveDiagnosisResponseDto.builder()
                .diagnosis(user.getRecords()
                        .stream()
                        .map(c -> modelMapper.map(c, SaveDiagnosisResponseDto.MainDiagnosisDto.class))
                        .collect(Collectors.toList()))
                .build();
    }


}