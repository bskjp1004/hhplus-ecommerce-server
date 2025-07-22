package kr.hhplus.be.server.config.error;

public record ErrorResponse(
        String code,
        String message
) {
}
