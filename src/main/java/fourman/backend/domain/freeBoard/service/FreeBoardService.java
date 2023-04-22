package fourman.backend.domain.freeBoard.service;

import fourman.backend.domain.freeBoard.controller.requestForm.FreeBoardRequestForm;
import fourman.backend.domain.freeBoard.entity.FreeBoard;
import fourman.backend.domain.freeBoard.service.responseForm.FreeBoardResponseForm;
import fourman.backend.domain.reviewBoard.controller.requestForm.ReviewBoardRequestForm;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FreeBoardService {
    public FreeBoard register(List<MultipartFile> fileList,
                              FreeBoardRequestForm freeBoardRequest);

    List<FreeBoardResponseForm> list();

    FreeBoardResponseForm read(Long boardId);

    void remove(Long boardId);

    Boolean modify(Long boardId, FreeBoardRequestForm boardRequest);

    List<FreeBoard> myPageList(Long memberId);

    List<FreeBoard> searchFreeBoardList(String searchText);

    Long incRecommendation(Long boardId);

    Long decRecommendation(Long boardId);
}
