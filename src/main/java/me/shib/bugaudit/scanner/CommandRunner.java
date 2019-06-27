package me.shib.bugaudit.scanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

final class CommandRunner {

    private Process process;
    private String command;
    private StreamProcessor inputProcessor;
    private StreamProcessor errorProcessor;
    private StringBuilder streamContent;
    private boolean showConsoleLog;

    CommandRunner(String command) {
        this.command = command;
        this.inputProcessor = new StreamProcessor(this, StreamType.INPUT);
        this.errorProcessor = new StreamProcessor(this, StreamType.ERROR);
        this.streamContent = new StringBuilder();
        this.inputProcessor.start();
        this.errorProcessor.start();
        this.showConsoleLog = true;
    }

    private synchronized void addLine(String line, StreamType type) {
        streamContent.append(line).append("\n");
        if (showConsoleLog) {
            System.out.println("[" + type + "] " + line);
        }
    }

    private Process getProcess() {
        return this.process;
    }

    void suppressConsoleLog() {
        this.showConsoleLog = false;
    }

    String getStreamContent() {
        return streamContent.toString();
    }

    String execute() throws IOException, InterruptedException {
        process = Runtime.getRuntime().exec(command);
        this.inputProcessor.join();
        this.errorProcessor.join();
        return streamContent.toString();
    }

    private enum StreamType {
        INPUT, ERROR
    }

    private final class StreamProcessor extends Thread {

        private CommandRunner commandRunner;
        private StreamType type;

        private StreamProcessor(CommandRunner commandRunner, StreamType type) {
            this.commandRunner = commandRunner;
            this.type = type;
        }

        private void processContent(InputStream inputStream) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                commandRunner.addLine(line, type);
            }
            reader.close();
        }

        @Override
        public void run() {
            Process process;
            while ((process = commandRunner.getProcess()) == null) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            InputStream inputStream = null;
            if (type == StreamType.INPUT) {
                inputStream = process.getInputStream();
            } else if (type == StreamType.ERROR) {
                inputStream = process.getErrorStream();
            }
            try {
                processContent(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
