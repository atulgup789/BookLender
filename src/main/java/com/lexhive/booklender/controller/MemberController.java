package com.lexhive.booklender.controller;

import com.lexhive.booklender.domain.Member;
import com.lexhive.booklender.dto.MemberRequest;
import com.lexhive.booklender.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/members")
@Tag(name = "Members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    @Operation(summary = "List  members")
    public List<Member> getMembers() {
        return memberService.getMembers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a member by id")
    public Member getMember(@PathVariable Long id) {
        return memberService.getMember(id);
    }

    @PostMapping
    @Operation(summary = "Register a member")
    public ResponseEntity<Member> createMember(@Valid @RequestBody MemberRequest request) {
        Member created = memberService.createMember(request);
        return ResponseEntity.created(URI.create("/members/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a member's name and email")
    public Member updateMember(@PathVariable Long id, @Valid @RequestBody MemberRequest request) {
        return memberService.updateMember(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a member who has no loans")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}
