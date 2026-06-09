package com.codemate.domain.studymember.service;

import com.codemate.domain.study.entity.Study;
import com.codemate.domain.study.repository.StudyRepository;
import com.codemate.domain.studymember.dto.MyStudyApplicationResponse;
import com.codemate.domain.studymember.dto.StudyMemberResponse;
import com.codemate.domain.studymember.entity.StudyMember;
import com.codemate.domain.studymember.entity.StudyMemberStatus;
import com.codemate.domain.studymember.repository.StudyMemberRepository;
import com.codemate.domain.user.entity.User;
import com.codemate.domain.user.repository.UserRepository;
import com.codemate.global.exception.BusinessException;
import com.codemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyMemberService {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final UserRepository userRepository;

    public List<StudyMemberResponse> getStudyMembers(Long hostId, Long studyId, StudyMemberStatus status) {
        Study study = findStudy(studyId);
        validateHost(study, hostId);

        List<StudyMember> studyMembers = status == null
                ? studyMemberRepository.findAllByStudy(study)
                : studyMemberRepository.findAllByStudyAndStatus(study, status);

        return studyMembers.stream()
                .map(StudyMemberResponse::from)
                .toList();
    }

    public List<MyStudyApplicationResponse> getMyStudyApplications(Long userId, StudyMemberStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<StudyMember> studyMembers = status == null
                ? studyMemberRepository.findAllByUserOrderByCreatedAtDesc(user)
                : studyMemberRepository.findAllByUserAndStatusOrderByCreatedAtDesc(user, status);

        return studyMembers.stream()
                .map(MyStudyApplicationResponse::from)
                .toList();
    }

    @Transactional
    public StudyMemberResponse apply(Long userId, Long studyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Study study = findStudyForUpdate(studyId);

        validateStudyApplicable(user, study);

        StudyMember studyMember = studyMemberRepository.findByStudyAndUser(study, user)
                .map(existingApplication -> reapply(existingApplication))
                .orElseGet(() -> StudyMember.builder()
                .study(study)
                .user(user)
                .status(StudyMemberStatus.PENDING)
                .build());

        return StudyMemberResponse.from(studyMemberRepository.save(studyMember));
    }

    @Transactional
    public StudyMemberResponse approve(Long hostId, Long studyId, Long memberId) {
        Study study = findStudyForUpdate(studyId);
        validateHost(study, hostId);

        StudyMember studyMember = findStudyMember(memberId);
        validateBelongsToStudy(studyMember, study);
        validatePending(studyMember);

        if (study.isFull()) {
            throw new BusinessException(ErrorCode.STUDY_CAPACITY_FULL);
        }

        studyMember.approve();
        study.increaseCurrentMemberCount();

        return StudyMemberResponse.from(studyMember);
    }

    @Transactional
    public StudyMemberResponse reject(Long hostId, Long studyId, Long memberId) {
        Study study = findStudy(studyId);
        validateHost(study, hostId);

        StudyMember studyMember = findStudyMember(memberId);
        validateBelongsToStudy(studyMember, study);
        validatePending(studyMember);

        studyMember.reject();

        return StudyMemberResponse.from(studyMember);
    }

    private void validateStudyApplicable(User user, Study study) {
        if (study.isHostedBy(user.getId())) {
            throw new BusinessException(ErrorCode.CANNOT_APPLY_OWN_STUDY);
        }

        if (!study.isRecruiting()) {
            throw new BusinessException(ErrorCode.STUDY_NOT_RECRUITING);
        }

        if (study.isFull()) {
            throw new BusinessException(ErrorCode.STUDY_CAPACITY_FULL);
        }
    }

    private StudyMember reapply(StudyMember studyMember) {
        if (!studyMember.isRejected()) {
            throw new BusinessException(ErrorCode.DUPLICATE_STUDY_APPLICATION);
        }

        studyMember.reapply();
        return studyMember;
    }

    private Study findStudy(Long studyId) {
        return studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));
    }

    private Study findStudyForUpdate(Long studyId) {
        return studyRepository.findByIdForUpdate(studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));
    }

    private StudyMember findStudyMember(Long memberId) {
        return studyMemberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_MEMBER_NOT_FOUND));
    }

    private void validateHost(Study study, Long userId) {
        if (!study.isHostedBy(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_HOST);
        }
    }

    private void validateBelongsToStudy(StudyMember studyMember, Study study) {
        if (!studyMember.getStudy().getId().equals(study.getId())) {
            throw new BusinessException(ErrorCode.STUDY_MEMBER_NOT_FOUND);
        }
    }

    private void validatePending(StudyMember studyMember) {
        if (!studyMember.isPending()) {
            throw new BusinessException(ErrorCode.INVALID_STUDY_MEMBER_STATUS);
        }
    }
}
