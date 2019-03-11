package me.shib.bugaudit.scanner;

import java.io.*;
import java.nio.charset.Charset;

/**
 * This is a wrapper over the Process Builder to provide easy execution of shell commands for the most common use cases.
 */
public final class CommandExecutor {

    private boolean displayConsoleOutput = false;
    private boolean dumpToFile;
    private StringBuilder consoleOutput;
    private File consoleOutputDumpFile;
    private String linePrefix;
    private Process p;

    /**
     * Instantiates a new CommandExecutor object.
     */
    public CommandExecutor() {
        linePrefix = "";
        consoleOutputDumpFile = new File("Console.log");
        dumpToFile = false;
        consoleOutput = new StringBuilder();
    }

    /**
     * Enables the output of the command execution to be printed in the console.
     *
     * @param showConsoleLogs enabling this will print the console output that the executing command will provide else it will be hidden.
     */
    public void enableConsoleOutput(boolean showConsoleLogs) {
        displayConsoleOutput = showConsoleLogs;
    }

    /**
     * This method will execute the given command or the set of commands and return the console output.
     *
     * @param commandArray the command array containing the list of commands to be executed.
     * @param envList      the list of environmental variables to be set. Should be stored as key=value format pairs in a String array.
     * @param dirPath      the working path that has to be used while running the command.
     * @return the console output of the executed commands.
     */
    public String runCommand(String[] commandArray, String[] envList, String dirPath) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < commandArray.length; i++) {
            sb.append(runCommand(commandArray[i], envList, dirPath)).append("\n");
        }
        return sb.toString();
    }

    /**
     * This method will execute the given command or the set of commands and return the console output.
     *
     * @param commandArray the command array containing the list of commands to be executed.
     * @param envList      the list of environmental variables to be set. Should be stored as key=value format pairs in a String array.
     * @return the console output of the executed commands.
     */
    public String runCommand(String[] commandArray, String[] envList) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < commandArray.length; i++) {
            sb.append(runCommand(commandArray[i], envList)).append("\n");
        }
        return sb.toString();
    }

    /**
     * This method will execute the given command or the set of commands and return the console output.
     *
     * @param commandArray the command array containing the list of commands to be executed.
     * @return the console output of the executed commands.
     */
    public String runCommand(String[] commandArray) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < commandArray.length; i++) {
            sb.append(runCommand(commandArray[i])).append("\n");
        }
        return sb.toString();
    }

    /**
     * This method will execute the given command and return the console output.
     *
     * @param command the command that has to be executed.
     * @param envList the list of environmental variables to be set. Should be stored as key=value format pairs in a String array.
     * @param dirPath the working path that has to be used while running the command.
     * @return the console output of the executed command.
     */
    public String runCommand(String command, String[] envList, String dirPath) {
        if (displayConsoleOutput) {
            System.out.println("Now Executing: " + command);
        }
        try {
            String line;
            p = Runtime.getRuntime().exec(command, envList, new File(dirPath));
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.defaultCharset()));
            if (dumpToFile) {
                File logFileParentDir = new File(consoleOutputDumpFile.getParent() + File.separator);
                if (!logFileParentDir.exists()) {
                    if (!logFileParentDir.mkdirs()) {
                        System.out.println("Unable to create the directory \"" + logFileParentDir.getPath() + "\".");
                    }
                }
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(consoleOutputDumpFile), Charset.defaultCharset()));
                while ((line = input.readLine()) != null) {
                    consoleOutput.append(linePrefix).append(line).append("\n");
                    pw.append(linePrefix).append(line).append("\n");
                    pw.flush();
                    if (displayConsoleOutput) {
                        System.out.println(linePrefix + line);
                    }
                }
                pw.close();
            } else {
                while ((line = input.readLine()) != null) {
                    consoleOutput.append(linePrefix).append(line).append("\n");
                    if (displayConsoleOutput) {
                        System.out.println(linePrefix + line);
                    }
                }
            }
            input.close();
            return consoleOutput.toString();
        } catch (FileNotFoundException e) {
            if (displayConsoleOutput) {
                System.out.println("Unable to find the  file to store the logs  :(");
            }
            return "";
        } catch (IOException e) {
            if (displayConsoleOutput) {
                System.out.println("Sorry, Something went wrong :(");
            }
            return "";
        }
    }

    /**
     * This method will execute the given command and return the console output.
     *
     * @param command the command that has to be executed.
     * @param envList the list of environmental variables to be set. Should be stored as key=value format pairs in a String array.
     * @return the console output of the executed command.
     */
    public String runCommand(String command, String[] envList) {
        if (displayConsoleOutput) {
            System.out.println("Now Executing: " + command);
        }
        try {
            String line;
            p = Runtime.getRuntime().exec(command, envList);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.defaultCharset()));
            if (dumpToFile) {
                File logFileParentDir = new File(consoleOutputDumpFile.getParent() + File.separator);
                if (!logFileParentDir.exists()) {
                    if (!logFileParentDir.mkdirs()) {
                        System.out.println("Unable to create the directory \"" + logFileParentDir.getPath() + "\".");
                    }
                }
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(consoleOutputDumpFile), Charset.defaultCharset()));
                while ((line = input.readLine()) != null) {
                    consoleOutput.append(linePrefix).append(line).append("\n");
                    pw.append(linePrefix).append(line).append("\n");
                    pw.flush();
                    if (displayConsoleOutput) {
                        System.out.println(linePrefix + line);
                    }
                }
                pw.close();
            } else {
                while ((line = input.readLine()) != null) {
                    consoleOutput.append(linePrefix).append(line).append("\n");
                    if (displayConsoleOutput) {
                        System.out.println(linePrefix + line);
                    }
                }
            }
            input.close();
            return consoleOutput.toString();
        } catch (FileNotFoundException e) {
            if (displayConsoleOutput) {
                System.out.println("Unable to find the  file to store the logs  :(");
            }
            return "";
        } catch (IOException e) {
            if (displayConsoleOutput) {
                System.out.println("Sorry, Something went wrong :(");
            }
            return "";
        }
    }

    /**
     * This method will execute the given command and return the console output.
     *
     * @param command the command that has to be executed.
     * @return the console output of the executed command.
     */
    public String runCommand(String command) {
        if (displayConsoleOutput) {
            System.out.println("Now Executing: " + command);
        }
        try {
            String line;
            p = Runtime.getRuntime().exec(command);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.defaultCharset()));
            if (dumpToFile) {
                File logFileParentDir = new File(consoleOutputDumpFile.getParent() + File.separator);
                if (!logFileParentDir.exists()) {
                    if (!logFileParentDir.mkdirs()) {
                        System.out.println("Unable to create the directory \"" + logFileParentDir.getPath() + "\".");
                    }
                }
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(consoleOutputDumpFile), Charset.defaultCharset()));
                while ((line = input.readLine()) != null) {
                    consoleOutput.append(linePrefix).append(line).append("\n");
                    pw.append(linePrefix).append(line).append("\n");
                    pw.flush();
                    if (displayConsoleOutput) {
                        System.out.println(linePrefix + line);
                    }
                }
                pw.close();
            } else {
                while ((line = input.readLine()) != null) {
                    consoleOutput.append(linePrefix).append(line).append("\n");
                    if (displayConsoleOutput) {
                        System.out.println(linePrefix + line);
                    }
                }
            }
            input.close();
            return consoleOutput.toString();
        } catch (FileNotFoundException e) {
            if (displayConsoleOutput) {
                System.out.println("Unable to find the  file to store the logs  :(");
            }
            return "";
        } catch (IOException e) {
            if (displayConsoleOutput) {
                System.out.println("Sorry, Something went wrong :(");
            }
            return "";
        }
    }

    /**
     * This method will execute the given command or the set of commands and return the console output.
     *
     * @param commandArray the command array containing the list of commands to be executed.
     * @param env          the environmental variable to be set. Should be provided as key=value format.
     * @param dirPath      the working path that has to be used while running the command.
     * @return the console output of the executed commands.
     */
    public String runCommand(String[] commandArray, String env, String dirPath) {
        String[] envList = {env};
        return runCommand(commandArray, envList, dirPath);
    }

    /**
     * This method will execute the given command or the set of commands and return the console output.
     *
     * @param commandArray the command array containing the list of commands to be executed.
     * @param env          the environmental variable to be set. Should be provided as key=value format.
     * @return the console output of the executed commands.
     */
    public String runCommand(String[] commandArray, String env) {
        String[] envList = {env};
        return runCommand(commandArray, envList);
    }

    /**
     * This method will execute the given command and return the console output.
     *
     * @param command the command that has to be executed.
     * @param env     the environmental variable to be set. Should be provided as key=value format.
     * @param dirPath the working path that has to be used while running the command.
     * @return the console output of the executed command.
     */
    public String runCommand(String command, String env, String dirPath) {
        String[] envList = {env};
        return runCommand(command, envList, dirPath);
    }

    /**
     * This method will execute the given command and return the console output.
     *
     * @param command the command that has to be executed.
     * @param env     the environmental variable to be set. Should be provided as key=value format.
     * @return the console output of the executed command.
     */
    public String runCommand(String command, String env) {
        String[] envList = {env};
        return runCommand(command, envList);
    }

    /**
     * Dumps the console output by creating a file in the provided file path.
     *
     * @param dumpFile the file to which the console output dump has to be created.
     */
    public void dumpOutputToFile(File dumpFile) {
        dumpToFile = true;
        consoleOutputDumpFile = dumpFile;
    }

    /**
     * Gives the console output of the executed commands from the initialization of the object.
     *
     * @return the console output of the executed commands from the initialization of the object.
     */
    public String getConsoleOutput() {
        return consoleOutput.toString();
    }

    /**
     * Appends a prefix to every line of the console output.
     *
     * @param linePrefix the prefix to append for every line in the console output..
     */
    public void setLinePrefix(String linePrefix) {
        this.linePrefix = linePrefix;
    }

    /**
     * Stops the execution of the current process.
     */
    public void stopExecution() {
        if (p != null) {
            p.destroy();
        }
    }
}
