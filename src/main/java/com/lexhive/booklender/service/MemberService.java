package com.lexhive.booklender.service;

import com.lexhive.booklender.domain.Member;
import com.lexhive.booklender.dto.MemberRequest;
import com.lexhive.booklender.repository.LoanRepository;
import com.lexhive.booklender.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;

    public MemberService(MemberRepository memberRepository, LoanRepository loanRepository) {
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
    }

    public List<Member> getMembers() {
        return memberRepository.findAll();
    }

    public Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
    }

    @Transactional
    public Member createMember(MemberRequest request) {
        if (memberRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A member with this email already exists");
        }
        Member saved = memberRepository.save(new Member(null, request.name(), request.email()));
        log.info("Created member {} ({})", saved.getId(), saved.getEmail());
        return saved;
    }

    @Transactional
    public Member updateMember(Long id, MemberRequest request) {
        Member member = getMember(id);
        if (memberRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A member with this email already exists");
        }
        member.setName(request.name());
        member.setEmail(request.email());
        Member saved = memberRepository.save(member);
        log.info("Updated member {}", saved.getId());
        return saved;
    }

    @Transactional
    public void deleteMember(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found");
        }
        if (loanRepository.existsByMemberId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Member has loans and cannot be deleted");
        }
        memberRepository.deleteById(id);
        log.info("Deleted member {}", id);
    }
}
