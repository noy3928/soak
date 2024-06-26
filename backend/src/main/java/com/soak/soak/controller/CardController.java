package com.soak.soak.controller;

import com.soak.soak.dto.card.CardDTO;
import com.soak.soak.dto.card.CardResponseDTO;


import com.soak.soak.repository.CardRepository;
import com.soak.soak.repository.CardTagMapRepository;
import com.soak.soak.repository.TagRepository;
import com.soak.soak.repository.UserRepository;
import com.soak.soak.service.CardService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.soak.soak.model.PageInfo;
import com.soak.soak.dto.card.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


import java.util.*;

@CrossOrigin(origins = "http://localhost:8080")
@RestController
@RequestMapping("/api")
public class CardController {

    @Autowired
    CardRepository cardRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    CardTagMapRepository cardTagMapRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private CardService cardService;

    private static final Logger logger = LoggerFactory.getLogger(CardController.class);

    @GetMapping("/cards")
    public ResponseEntity<PagedResponse<CardResponseDTO>> getCards(@RequestParam Optional<String> tag,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size) {
        try {
            Page<CardResponseDTO> pageCards;
            if (tag.isPresent()) {
                pageCards = cardService.getCardsByTag(tag.get(), PageRequest.of(page, size));
            } else {
                pageCards = cardService.getAllCards(PageRequest.of(page, size));
            }

            PageInfo pageInfo = new PageInfo(
                    pageCards.getNumber(),
                    pageCards.getSize(),
                    pageCards.getTotalElements(),
                    pageCards.getTotalPages()
            );

            PagedResponse<CardResponseDTO> response = new PagedResponse<>(pageInfo, pageCards.getContent());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/cards/user/{userId}")
    public ResponseEntity<PagedResponse<CardResponseDTO>> getCardsByUserId(@PathVariable UUID userId,
                                                                           @RequestParam Optional<String> tag,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "10") int size) {
        try {
            Page<CardResponseDTO> pageCards;

            if (tag.isPresent()) {
                pageCards = cardService.getPublicCardsByTagAndUserId(userId, tag.get(), PageRequest.of(page, size));
            } else {
                pageCards = cardService.getCardsByUserId(userId, PageRequest.of(page, size));
            }

            PageInfo pageInfo = new PageInfo(
                    pageCards.getNumber(),
                    pageCards.getSize(),
                    pageCards.getTotalElements(),
                    pageCards.getTotalPages()
            );

            PagedResponse<CardResponseDTO> response = new PagedResponse<>(pageInfo, pageCards.getContent());
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/cards/{id}")
    public ResponseEntity<CardResponseDTO> getCardById(@PathVariable UUID id) {
        try {
            CardResponseDTO cardResponseDTO = cardService.getCardById(id);
            return new ResponseEntity<>(cardResponseDTO, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/cards/search")
    public ResponseEntity<List<CardResponseDTO>> searchCards(@RequestParam String query) {
        try {
            List<CardResponseDTO> cardResponseDTOs = cardService.searchCards(query);
            return new ResponseEntity<>(cardResponseDTOs, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error searching cards with query: {}", query, e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/cards")
    public ResponseEntity<CardResponseDTO> createCard(@RequestBody CardDTO cardDTO) {
        try {
            CardResponseDTO cardResponseDTO = cardService.createCard(cardDTO);
            return new ResponseEntity<>(cardResponseDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating card with question: {}", cardDTO.getQuestion(), e);
            logger.error("Error creating card with answer: {}", cardDTO.getAnswer(), e);
            logger.error("Error creating card with tags: {}", cardDTO.getTags(), e);
            logger.error("Error creating card with isPublic: {}", cardDTO.isPublic(), e);

            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/cards/{id}/copy")
    public ResponseEntity<CardResponseDTO> copyCard(@PathVariable UUID id) {
        try {
            CardResponseDTO cardResponseDTO = cardService.copyCard(id);
            return new ResponseEntity<>(cardResponseDTO, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            logger.error("Card not found with id: {}", id, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error copying card with id: {}", id, e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/cards/{id}")
    public ResponseEntity<CardResponseDTO> updateCard(@PathVariable UUID id, @RequestBody CardDTO cardDTO) {
        try {
            CardResponseDTO updatedCardResponseDTO = cardService.updateCard(id, cardDTO);
            return new ResponseEntity<>(updatedCardResponseDTO, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable UUID id) {
        try {
            cardService.deleteCard(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
