package com.course.krch.qrclub.exception;

import java.util.List;

public record ValidationErrorResponse(List<Violation> violations) {
}
