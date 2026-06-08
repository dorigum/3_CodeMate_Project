package com.codemate.domain.study.service;

import com.codemate.domain.study.dto.StudyCreateRequest;
import com.codemate.domain.study.dto.StudyResponse;
import com.codemate.domain.study.dto.StudySearchCondition;
import com.codemate.domain.study.dto.StudySummaryResponse;
import com.codemate.domain.study.dto.StudyUpdateRequest;
import com.codemate.domain.study.entity.Study;
import com.codemate.domain.study.entity.StudyStatus;
import com.codemate.domain.study.repository.StudyRepository;
import com.codemate.domain.study.repository.StudySpecifications;
import com.codemate.domain.studymember.repository.StudyMemberRepository;
import com.codemate.domain.techstack.entity.StudyTechStack;
import com.codemate.domain.techstack.entity.TechStack;
import com.codemate.domain.techstack.repository.StudyTechStackRepository;
import com.codemate.domain.techstack.repository.TechStackRepository;
import com.codemate.domain.user.entity.User;
import com.codemate.domain.user.repository.UserRepository;
import com.codemate.global.exception.BusinessException;
import com.codemate.global.exception.ErrorCode;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyService {

    private final StudyRepository studyRepository;
    private final UserRepository userRepository;
    private final TechStackRepository techStackRepository;
    private final StudyTechStackRepository studyTechStackRepository;
    private final StudyMemberRepository studyMemberRepository;

    @Transactional
    public StudyResponse createStudy(Long userId, StudyCreateRequest request) {
        User host = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Study study = Study.builder()
                .host(host)
                .title(request.title())
                .content(request.content())
                .category(request.category())
                .meetingType(request.meetingType())
                .location(request.location())
                .maxMemberCount(request.maxMemberCount())
                .currentMemberCount(1)
                .status(StudyStatus.RECRUITING)
                .build();

        Study savedStudy = studyRepository.save(study);
        syncTechStacks(savedStudy, request.techStackNames());

        return StudyResponse.from(savedStudy, getTechStackNames(savedStudy));
    }

    public Page<StudySummaryResponse> getStudies(StudySearchCondition condition, Pageable pageable) {
        return studyRepository.findAll(StudySpecifications.withCondition(condition), pageable)
                .map(study -> StudySummaryResponse.from(study, getTechStackNames(study)));
    }

    public StudyResponse getStudy(Long studyId) {
        Study study = findStudy(studyId);
        return StudyResponse.from(study, getTechStackNames(study));
    }

    @Transactional
    public StudyResponse updateStudy(Long userId, Long studyId, StudyUpdateRequest request) {
        Study study = findStudy(studyId);
        validateHost(study, userId);
        validateCapacity(study, request.maxMemberCount());

        study.update(
                request.title(),
                request.content(),
                request.category(),
                request.meetingType(),
                request.location(),
                request.maxMemberCount(),
                request.status()
        );
        syncTechStacks(study, request.techStackNames());

        return StudyResponse.from(study, getTechStackNames(study));
    }

    @Transactional
    public void deleteStudy(Long userId, Long studyId) {
        Study study = findStudy(studyId);
        validateHost(study, userId);
        studyMemberRepository.deleteAllByStudy(study);
        studyTechStackRepository.deleteAllByStudy(study);
        studyMemberRepository.flush();
        studyTechStackRepository.flush();
        studyRepository.delete(study);
    }

    private Study findStudy(Long studyId) {
        return studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));
    }

    private void validateHost(Study study, Long userId) {
        if (!study.isHostedBy(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_HOST);
        }
    }

    private void validateCapacity(Study study, int maxMemberCount) {
        if (maxMemberCount < study.getCurrentMemberCount()) {
            throw new BusinessException(ErrorCode.INVALID_STUDY_CAPACITY);
        }
    }

    private void syncTechStacks(Study study, List<String> techStackNames) {
        studyTechStackRepository.deleteAllByStudy(study);
        studyTechStackRepository.flush();

        List<StudyTechStack> studyTechStacks = normalizeTechStackNames(techStackNames)
                .stream()
                .map(this::getOrCreateTechStack)
                .map(techStack -> StudyTechStack.builder()
                        .study(study)
                        .techStack(techStack)
                        .build())
                .toList();

        studyTechStackRepository.saveAll(studyTechStacks);
    }

    private TechStack getOrCreateTechStack(String name) {
        return techStackRepository.findByName(name)
                .orElseGet(() -> techStackRepository.save(TechStack.builder()
                        .name(name)
                        .build()));
    }

    private List<String> getTechStackNames(Study study) {
        return studyTechStackRepository.findAllByStudy(study)
                .stream()
                .map(studyTechStack -> studyTechStack.getTechStack().getName())
                .toList();
    }

    private List<String> normalizeTechStackNames(List<String> techStackNames) {
        if (techStackNames == null) {
            return List.of();
        }

        return techStackNames.stream()
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
    }
}
