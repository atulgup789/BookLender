package com.lexhive.booklender.service;

import com.lexhive.booklender.domain.Member;
import com.lexhive.booklender.dto.MemberRequest;
import com.lexhive.booklender.repository.LoanRepository;
import com.lexhive.booklender.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private LoanRepository loanRepository;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository, loanRepository);
    }

    @Test
    void createMemberStoresATrimmedLowercaseEmail() {
        when(memberRepository.findByEmail("ada@example.com")).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Member created = memberService.createMember(new MemberRequest(" Ada Lovelace ", " Ada@Example.com "));

        assertThat(created.getName()).isEqualTo("Ada Lovelace");
        assertThat(created.getEmail()).isEqualTo("ada@example.com");
    }
}
