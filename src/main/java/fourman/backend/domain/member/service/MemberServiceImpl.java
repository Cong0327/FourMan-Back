package fourman.backend.domain.member.service;
import fourman.backend.domain.member.entity.*;
import fourman.backend.domain.member.repository.*;
import fourman.backend.domain.member.service.request.EmailMatchRequest;
import fourman.backend.domain.member.service.request.EmailPasswordRequest;
import fourman.backend.domain.member.service.request.MemberLoginRequest;
import fourman.backend.domain.member.service.request.MemberRegisterRequest;
import fourman.backend.domain.member.service.response.MemberLoginResponse;
import fourman.backend.domain.security.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

    final private MemberRepository memberRepository;
    final private ManagerCodeRepository managerCodeRepository;
    final private CafeCodeRepository cafeCodeRepository;
    final private AuthenticationRepository authenticationRepository;
    final private RedisService redisService;
    final private PointRepository pointRepository;

    @Override
    public Boolean emailValidation(String email) {
        Optional<Member> maybeMember = memberRepository.findByEmail(email);
        if (maybeMember.isPresent()) {//이메일이 존재한다면
            return false;//false면 중복된게 있다는말
        }
        return true;
    }
    @Override
    public Boolean memberNicknameValidation(String nickName) {
        Optional<Member> maybeMemberNickname = memberRepository.findByNickName(nickName);

        if (maybeMemberNickname.isPresent()) {
            return false;
        }
        return true;
    }


    @Override
    public Boolean managerCodeValidation(String managerCode) {
        Optional<ManagerCode> maybeManager = managerCodeRepository.findByCode(managerCode);
        if (maybeManager.isPresent()) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean cafeCodeValidation(String cafeCode) {
        Optional<CafeCode> maybeCafe = cafeCodeRepository.findByCode(cafeCode);
        if (maybeCafe.isPresent()) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean signUp(MemberRegisterRequest memberRegisterRequest) {
        final Member member = memberRegisterRequest.toMember();

        if(member.getAuthority().getAuthorityName().equals(AuthorityType.MEMBER)) {
            Point point = new Point(1000l, member);
            member.setPoint(point);
        }

        memberRepository.save(member);
        final BasicAuthentication authentication = new BasicAuthentication(
                member,
                Authentication.BASIC_AUTH,//authenticationType칼럼에 값을 멤버변수 BASIC_AUTH의 값을 넣음
                memberRegisterRequest.getPassword()
        );
        authenticationRepository.save(authentication);
        return true;
    }


    @Override
    public MemberLoginResponse signIn(MemberLoginRequest memberLoginRequest) {
        try {
        Optional<Member> maybeMember =
                memberRepository.findByEmail(memberLoginRequest.getEmail());

        System.out.println("loginRequest: " + memberLoginRequest);
        System.out.println("maybeMember.isPresent(): " + maybeMember.isPresent());

        if (maybeMember.isPresent()) {//이메일이 존재할경우
            Member member = maybeMember.get();

            System.out.println("사용자가 입력한 비번: " + memberLoginRequest.getPassword());
            System.out.println("비밀번호 일치 검사: " + member.isRightPassword(memberLoginRequest.getPassword()));
            if (!member.isRightPassword(memberLoginRequest.getPassword())) {
                System.out.println("비밀번호가 맞지 않습니다. ?");
                throw new RuntimeException("이메일 및 비밀번호 입력이 잘못되었습니다!");
                //있는 이메일인데 비번이 잘못입력됨
            }
            //비번이 제대로 입력됨(로그인성공)
            UUID userToken = UUID.randomUUID();

            // redis 처리 필요
            redisService.deleteByKey(userToken.toString());
            redisService.setKeyAndValue(userToken.toString(), member.getId());
            //레디스에 토큰:유저ID 입력

            MemberLoginResponse memberLoginResponse=null;
            System.out.println("member.getAuthority().getAuthorityName(): "+member.getAuthority().getAuthorityName());

            if(member.getAuthority().getAuthorityName().equals(AuthorityType.CAFE)){ //카페사업자의 경우카페명 UI로 보내줌
                log.info("카페사업자 입니다!");
                Optional<CafeCode> op= cafeCodeRepository.findByCode(member.getCode());
                System.out.println("op"+op);
                System.out.println("op.get()"+op.get());
                System.out.println("op.get().getCafe():"+op.get().getCafe());
                if(op.isPresent()){
                    if(op.get().getCafe()==null){
                        memberLoginResponse = new MemberLoginResponse(userToken.toString(), member.getId(), member.getNickName(), member.getAuthority().getAuthorityName(), null ,member.getCode(),op.get().getCafeName(), member.getEmail());
                        log.info("카페를 아직 등록하지않음.");
                        return memberLoginResponse;
                    }
                    System.out.println("op.get().getCafe().getCafeId():"+op.get().getCafe().getCafeId());
                    memberLoginResponse = new MemberLoginResponse(userToken.toString(),member.getId(),member.getNickName(), member.getAuthority().getAuthorityName(),op.get().getCafe().getCafeId(), member.getCode(),op.get().getCafeName(), member.getEmail());
                }
                else{
                    log.info("카페코드를 못찾았습니다");
                }
            }else {
                log.info("카페사업자가 아닙니다.");
                memberLoginResponse = new MemberLoginResponse(userToken.toString(), member.getId(), member.getNickName(), member.getAuthority().getAuthorityName(), null ,member.getCode(), null, member.getEmail());
            }
            return memberLoginResponse;
        }

            throw new RuntimeException("가입된 사용자가 아닙니다!");
        } catch (NullPointerException e) {
            System.out.println("NullPointerException 발생!");
            e.printStackTrace();
            throw new RuntimeException("NullPointerException이 발생했습니다. 로그를 확인하세요.");
        }
    }
    @Override
    public Boolean applyNewPassword(EmailPasswordRequest emailPasswordRequest) {
        Optional<Authentication> maybeAuthentication = authenticationRepository.findByEmail(emailPasswordRequest.getEmail());
        if (!maybeAuthentication.isPresent()){ //인증정보가 존재하지 않을 경우
            System.out.println("인증정보가 존재하지않습니다.");
            return false;
        }
        BasicAuthentication authentication = (BasicAuthentication)maybeAuthentication.get();
        authentication.setPassword(emailPasswordRequest.getPassword());
        authenticationRepository.save(authentication);
        System.out.println("비밀번호 변경완료");

        return true;
    }

    @Override
    public Boolean emailMatch(EmailMatchRequest toEmailMatchRequest) {
        Optional<Member> maybeMember = memberRepository.findByEmail(toEmailMatchRequest.getEmail());
        if (!maybeMember.isPresent()){//이메일이 존재하지 않을경우
            return false;
        }

        return true;//이미존재하는 이메일
    }

}