package com.example.AIHealth.service;

import com.example.AIHealth.dto.BoardDTO;
import com.example.AIHealth.dto.BoardImageDTO;
import com.example.AIHealth.entity.BoardEntity;
import com.example.AIHealth.entity.BoardImageEntity;
import com.example.AIHealth.entity.BoardRecommendEntity;
import com.example.AIHealth.entity.MemberEntity;
import com.example.AIHealth.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final BoardRecommendRepository boardRecommendRepository;
    private final BoardImageRepository boardImageRepository;

    // 게시글 저장 (이미지 없는 경우)
    public void save(BoardDTO dto, Long loginMemberId) {
        MemberEntity member = memberRepository.findById(loginMemberId)
                .orElseThrow(() -> new IllegalArgumentException("로그인한 회원을 찾을 수 없습니다."));

        BoardEntity entity = new BoardEntity();
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setMember(member);
        entity.setMemberName(member.getMemberName());
        entity.setViewCount(0);
        entity.setRecommendCount(0);

        boardRepository.save(entity);
    }

    // 게시글 저장 + Base64 이미지
    @Transactional
    public void saveWithBase64Images(BoardDTO dto, Long loginMemberId, String imageData) {
        MemberEntity member = memberRepository.findById(loginMemberId)
                .orElseThrow(() -> new IllegalArgumentException("로그인한 회원을 찾을 수 없습니다."));

        BoardEntity board = new BoardEntity();
        board.setTitle(dto.getTitle());
        board.setContent(dto.getContent());
        board.setMember(member);
        board.setMemberName(member.getMemberName());
        board.setViewCount(0);
        board.setRecommendCount(0);

        boardRepository.save(board);

        if (imageData != null && !imageData.isEmpty()) {
            saveImagesFromBase64(board, imageData);
        }
    }

    @Transactional
    public void updateWithBase64Images(Long id, String title, String content, String imageData, String deleteImageIdsString) {
        BoardEntity board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));

        board.setTitle(title);
        board.setContent(content);

        // 1. 기존 이미지 삭제 처리
        final List<Long> parsedDeleteImageIds = new ArrayList<>();
        if (deleteImageIdsString != null && !deleteImageIdsString.isEmpty()) {
            try {
                parsedDeleteImageIds.addAll(Arrays.stream(deleteImageIdsString.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(Collectors.toList()));
            } catch (NumberFormatException e) {
                System.err.println("Error parsing deleteImageIds: " + e.getMessage());
            }
        }

        if (!parsedDeleteImageIds.isEmpty()) {
            List<BoardImageEntity> imagesToDelete = board.getImages().stream()
                    .filter(img -> parsedDeleteImageIds.contains(img.getId()))
                    .collect(Collectors.toList());

            for (BoardImageEntity img : imagesToDelete) {
                // 실제 파일 삭제
                File file = new File("uploads/board/" + img.getFileName());
                if (file.exists()) file.delete();

                // DB에서 삭제
                boardImageRepository.delete(img);
                board.removeImage(img);
            }
        }

        // 2. 새로운 이미지 추가
        if (imageData != null && !imageData.isEmpty()) {
            saveImagesFromBase64(board, imageData);
        }

        boardRepository.save(board);
    }

    // Base64 이미지 저장 공통 메서드
    @Transactional
    public void saveImagesFromBase64(BoardEntity board, String imageData) {
        ObjectMapper mapper = new ObjectMapper();
        List<String> imageList;
        try {
            imageList = mapper.readValue(imageData, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        for (String base64 : imageList) {
            try {
                String[] parts = base64.split(",");
                String imageString = parts.length > 1 ? parts[1] : parts[0];
                byte[] imageBytes = Base64.getDecoder().decode(imageString);

                String fileName = System.currentTimeMillis() + "_" + (int)(Math.random() * 1000) + ".png";
                String uploadDir = "uploads/board/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                File imageFile = new File(dir, fileName);
                try (OutputStream os = new FileOutputStream(imageFile)) {
                    os.write(imageBytes);
                }

                BoardImageEntity boardImage = new BoardImageEntity();
                boardImage.setBoard(board);
                boardImage.setFileName(fileName);
                boardImage.setFilePath("/board/uploads/board/" + fileName);
                boardImageRepository.save(boardImage);

                board.addImage(boardImage);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 페이지 조회 + 댓글 수 + 이미지 DTO
    public Page<BoardDTO> getBoardPageWithCommentCountAndSort(String keyword, int page, int size, String sort) {
        Sort sortOrder = "recommend".equals(sort)
                ? Sort.by(Sort.Direction.DESC, "recommendCount", "createdAt")
                : Sort.by(Sort.Direction.DESC, "createdAt");

        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Page<BoardEntity> boardEntities = (keyword == null || keyword.trim().isEmpty())
                ? boardRepository.findAll(pageable)
                : boardRepository.findByTitleContaining(keyword, pageable);

        List<Long> boardIds = boardEntities.stream()
                .filter(Objects::nonNull)
                .map(BoardEntity::getId)
                .collect(Collectors.toList());

        List<Object[]> commentCountList = boardIds.isEmpty() ? new ArrayList<>() : commentRepository.countCommentsByBoardIds(boardIds);
        Map<Long, Long> commentCountMap = commentCountList.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        List<BoardDTO> boardDTOList = boardEntities.stream()
                .filter(Objects::nonNull)
                .map(board -> {
                    BoardDTO dto = new BoardDTO(board);
                    dto.setCommentCount(commentCountMap.getOrDefault(board.getId(), 0L));

                    List<BoardImageDTO> images = board.getImages().stream()
                            .map(BoardImageDTO::new)
                            .collect(Collectors.toList());
                    dto.setImages(images);

                    return dto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(boardDTOList, pageable, boardEntities.getTotalElements());
    }

    public List<BoardDTO> findAll() {
        List<BoardEntity> boardEntities = boardRepository.findAllWithMember();

        return boardEntities.stream()
                .filter(Objects::nonNull)
                .map(BoardDTO::new)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<BoardDTO> findRecentPosts(int count) {
        Pageable pageable = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BoardEntity> boardEntities = boardRepository.findAll(pageable);

        return boardEntities.getContent().stream()
                .map(BoardDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public BoardDTO findById(Long id) {
        BoardEntity boardEntity = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));
        boardEntity.setViewCount(boardEntity.getViewCount() + 1);

        BoardDTO dto = new BoardDTO(boardEntity);

        // null-safe 처리
        List<BoardImageDTO> images = Optional.ofNullable(boardEntity.getImages())
                .orElse(Collections.emptyList())
                .stream()
                .map(BoardImageDTO::new)
                .collect(Collectors.toList());

        dto.setImages(images);
        return dto;
    }

    @Transactional
    public void delete(Long boardId) {
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));

        // 게시글에 연결된 이미지 파일들을 먼저 삭제
        for (BoardImageEntity img : board.getImages()) {
            File file = new File("uploads/board/" + img.getFileName());
            if (file.exists()) {
                file.delete();
            }
        }

        boardRecommendRepository.deleteAllByBoardId(boardId);
        commentRepository.deleteByBoardId(boardId);
        boardRepository.deleteById(boardId);
    }

    @Transactional
    public int toggleRecommend(Long boardId, Long memberId) {
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        boolean alreadyRecommended = boardRecommendRepository.existsByBoardIdAndMemberId(boardId, memberId);

        if (alreadyRecommended) {
            boardRecommendRepository.deleteByBoardIdAndMemberId(boardId, memberId);
            board.setRecommendCount(board.getRecommendCount() - 1);
        } else {
            MemberEntity member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
            BoardRecommendEntity recommend = new BoardRecommendEntity();
            recommend.setBoard(board);
            recommend.setMember(member);
            boardRecommendRepository.save(recommend);

            board.setRecommendCount(board.getRecommendCount() + 1);
        }

        boardRepository.save(board);
        return board.getRecommendCount();
    }
}