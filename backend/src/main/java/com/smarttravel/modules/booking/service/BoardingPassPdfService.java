package com.smarttravel.modules.booking.service;

import com.smarttravel.modules.booking.model.BoardingPass;

import java.util.List;

/**
 * Service for generating dynamic vector PDF Boarding Passes.
 */
public interface BoardingPassPdfService {

    byte[] generateBoardingPassPdf(BoardingPass boardingPass);

    byte[] generateMultiBoardingPassPdf(List<BoardingPass> boardingPasses);
}
