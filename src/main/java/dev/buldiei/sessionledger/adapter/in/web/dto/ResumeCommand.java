package dev.buldiei.sessionledger.adapter.in.web.dto;

/** Builds the copy-paste CLI command that re-opens a session in its original directory. */
final class ResumeCommand {

    private ResumeCommand() {
    }

    static String of(String projectDir, String sessionId) {
        return "cd " + shellQuote(projectDir) + " && claude --resume " + sessionId;
    }

    private static String shellQuote(String value) {
        // single-quote and escape embedded single quotes for POSIX shells
        return "'" + value.replace("'", "'\\''") + "'";
    }
}
