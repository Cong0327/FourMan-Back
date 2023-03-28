package fourman.backend.domain.reviewBoard.service;

import fourman.backend.domain.reviewBoard.controller.requestForm.ReviewBoardRequestForm;
import fourman.backend.domain.reviewBoard.entity.ReviewBoard;
import fourman.backend.domain.reviewBoard.entity.ReviewBoardImageResource;
import fourman.backend.domain.reviewBoard.repository.ReviewBoardImageResourceRepository;
import fourman.backend.domain.reviewBoard.repository.ReviewBoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewBoardServiceImpl implements ReviewBoardService {

    final private ReviewBoardRepository reviewBoardRepository;

    final private ReviewBoardImageResourceRepository reviewBoardImageResourceRepository;

    @Transactional
    @Override
    public void register(List<MultipartFile> fileList, ReviewBoardRequestForm reviewBoardRequest) {
        log.info("글자 출력: " + reviewBoardRequest);

        List<ReviewBoardImageResource> reviewBoardImageResourceList = new ArrayList<>();

        // 현재 경로를 기준으로 프론트 엔드의 uploadImgs로 상대경로 값을 문자열로 저장함 (파일을 저장할 경로)
        final String fixedStringPath = "../../FourMan-Front/frontend/src/assets/reviewImage/";

        ReviewBoard reviewBoard = new ReviewBoard();

        // 받아온 상품정보 값 setting
        reviewBoard.setCafeName(reviewBoardRequest.getCafeName());
        reviewBoard.setWriter(reviewBoardRequest.getWriter());
        reviewBoard.setContent(reviewBoardRequest.getContent());
        reviewBoard.setRating(reviewBoardRequest.getRating());
        reviewBoard.setMemberId(reviewBoardRequest.getMemberId());

        if(fileList != null) {
            try {
                for (MultipartFile multipartFile: fileList) {
                    log.info("requestFileUploadWithText() - filename: " + multipartFile.getOriginalFilename());

                    // 파일 저장 위치에 파일 이름을 더해 fullPath 문자열 저장
                    String fullPath = fixedStringPath + multipartFile.getOriginalFilename();


                    FileOutputStream writer = new FileOutputStream(fullPath);

                    writer.write(multipartFile.getBytes());
                    writer.close();

                    // 이미지 경로를 DB에 저장할때 경로를 제외한 이미지파일 이름만 저장하도록 함 (프론트에서 경로 지정하여 사용하기 위함)
                    ReviewBoardImageResource reviewBoardImageResource = new ReviewBoardImageResource(multipartFile.getOriginalFilename());
                    reviewBoardImageResourceList.add(reviewBoardImageResource);
                    reviewBoard.setReviewBoardImageResource(reviewBoardImageResource);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            reviewBoardImageResourceRepository.saveAll(reviewBoardImageResourceList);
        }

        reviewBoardRepository.save(reviewBoard);
    }
}
