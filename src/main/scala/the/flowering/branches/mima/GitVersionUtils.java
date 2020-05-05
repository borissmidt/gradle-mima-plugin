/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 *  v. 2.0. If a copy of the MPL was not distributed with this file,
 *  You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package the.flowering.branches.mima;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.gradle.api.Project;
import org.gradle.process.ExecResult;

final class GitVersionUtils {
    private GitVersionUtils() {
    }

    public static Stream<String> previousGitTags(Project project) {
        return StreamSupport.stream(new PreviousGitTags(project), false)
                .filter(tag -> !isInitial000Tag(project, tag))
                .map(GitVersionUtils::stripVFromTag);
    }

    private static Optional<String> previousGitTagFromRef(Project project, String ref) {
        String beforeLastRef = ref + "^";

        GitResult beforeLastRefTypeResult = execute(project, "git", "cat-file", "-t", beforeLastRef);

        boolean thereIsNoCommitBeforeTheRef = !beforeLastRefTypeResult.stdout.equals("commit");
        if (thereIsNoCommitBeforeTheRef) {
            return Optional.empty();
        }

        GitResult describeResult = execute(project, "git", "describe", "--tags", "--abbrev=0", beforeLastRef);

        if (describeResult.stderr.contains("No tags can describe")) {
            return Optional.empty();
        }

        return Optional.of(describeResult.stdoutOrThrowIfNonZero());
    }

    private static boolean isInitial000Tag(Project project, String tag) {
        if (!tag.equals("0.0.0")) {
            return false;
        }

        GitResult foo = execute(project, "git", "rev-parse", "--verify", "--quiet", "0.0.0^");
        boolean parentDoesNotExist = foo.exitCode != 0;
        return parentDoesNotExist;
    }

    private static String stripVFromTag(String tag) {
        if (tag.startsWith("v")) {
            return tag.substring(1);
        } else {
            return tag;
        }
    }

    private static GitResult execute(Project project, String... command) {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        ExecResult execResult = project.exec(spec -> {
            spec.setCommandLine(Arrays.asList(command));
            spec.setStandardOutput(stdout);
            spec.setErrorOutput(stderr);
            spec.setIgnoreExitValue(true);
        });

        return new GitResult(
                execResult.getExitValue(),
                new String(stdout.toByteArray(), StandardCharsets.UTF_8).trim(),
                new String(stderr.toByteArray(), StandardCharsets.UTF_8).trim(),
                Collections.EMPTY_LIST);
    }

    static class GitResult {
        public final int exitCode;
        public final String stdout;
        public final String stderr;
        public final List<String> command;

        public GitResult(int exitCode, String stdout, String stderr, List<String> command) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
            this.command = command;
        }

        String stdoutOrThrowIfNonZero() {
            if (exitCode == 0) {
                return stdout;
            }

            throw new RuntimeException("Failed running command:\n"
                    + "\tCommand:" + command + "\n"
                    + "\tExit code: " + exitCode + "\n"
                    + "\tStdout:" + stdout + "\n"
                    + "\tStderr:" + stderr + "\n");
        }

    }

    private static final class PreviousGitTags implements Spliterator<String> {
        private final Project project;
        private String lastSeenRef = "HEAD";

        PreviousGitTags(Project project) {
            this.project = project;
        }

        @Override
        public boolean tryAdvance(Consumer<? super String> action) {
            Optional<String> tag = previousGitTagFromRef(project, lastSeenRef);

            if (!tag.isPresent()) {
                return false;
            }

            lastSeenRef = tag.get();
            action.accept(lastSeenRef);
            return true;
        }

        @Override
        public Spliterator<String> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return 0;
        }
    }
}