package test.implementation;

import api.documentation.Connection;
import api.documentation.Manager;
import api.documentation.UserPrivilege;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        String type = null;
        Manager manager = null;
        String path = null;
        String[] un_pass;
        Scanner sc = new Scanner(System.in);

        //Local or Remote
        while (true) {
            System.out.println("Connect to local or remote storage (local/remote)");
            type = sc.nextLine();
            if (type.equals("local")) {
                manager = local.implemetation.ManagerImplementation.getInstance();
                System.out.println("Connected to local storage!");
                break;
            } else if (type.equals("remote")) {
                manager = remote.implementation.ManagerImplementation.getInstance();
                System.out.println("Connected to remote storage!");
                break;
            } else {
                System.out.println("Unknown command!");
            }
        }
        //Input root path, username and path
        System.out.println("Input root path!");
        path = sc.nextLine();
        Connection connection = null;
        while (true) {
            System.out.println("Input username and password divided with space (ex. admin 123)");
            un_pass = sc.nextLine().split(" ");
            try {
                connection = manager.connect(path, un_pass[0], un_pass[1]);
                if (connection.isLoggedIn()) break;
            }catch (IndexOutOfBoundsException e){
                System.out.println("Oops you didn't give enough arguments.");
            }
        }
        System.out.println("You are ready to go! If you need help just type \"help\" command to display all commands\n" +
                "Or \"help\" <command> to see what that command does.\nType \"exit\" to close the program");
        while (true) {
            String command;
            String arguments;
            String line;
            String[] parts = null;
            line = sc.nextLine();
            if (line.equalsIgnoreCase("exit")) {
                break;
            }
            try {
                if (line.contains(" ")){
                    //TODO ovo prebaciti na metodu doCommand da prima String... kao komandu i argumente pa tamo proveravati
                    parts = line.split("\\s+", 2);
                    command = parts[0];
                    arguments = parts[1];
                    if (!doCommand(connection, command, arguments)) {
                        System.out.println("Unknown command!");
                    }
                }else{
                    if (!doCommand(connection, line, "")) {
                        System.out.println("Unknown command!");
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Oops you didn't give enough arguments.");
            }
        }
    }

    private static boolean doCommand(Connection connection, String command, String arguments) {
        if (command.equals("upload")) {
            if (!connection.isAdmin())
                System.out.println("This command is only for admin!");
            else
                connection.upload(arguments);
        } else if (command.equals("download")) {
            connection.download(arguments);
        } else if (command.equals("set_meta")) {
            String[] parts = arguments.split(" ");
            connection.set_meta(parts[0], parts[1], parts[2]);
        } else if (command.equals("addUser")) {
            if (!connection.isAdmin()) {
                System.out.println("This command is only for admin!");
                return true;
            }
            String[] parts = arguments.split(" ");
            UserPrivilege userPrivilege = null;
            if (parts[2].equalsIgnoreCase("ADMIN")) {
                userPrivilege = UserPrivilege.ADMIN;
            } else if (parts[2].equalsIgnoreCase("GUEST")) {
                userPrivilege = UserPrivilege.GUEST;
            } else {
                System.out.println("Privilege is not set appropriately.");
                return true;
            }
            connection.addUser(parts[0], parts[1], userPrivilege);
        } else if (command.equals("mkDir")) {
            if (!connection.isAdmin()) {
                System.out.println("This command is only for admin!");
                return true;
            }
            String[] parts = arguments.split(" ");
            connection.mkDir(parts[0], parts[1]);
        } else if (command.equals("mkFile")) {
            if (!connection.isAdmin()) {
                System.out.println("This command is only for admin!");
                return true;
            }
            connection.mkFile(arguments);
        } else if (command.equals("deleteItem")) {
            if (!connection.isAdmin()) {
                System.out.println("This command is only for admin!");
                return true;
            }
            connection.deleteItem(arguments);
        } else if (command.equals("isBlacklisted")) {
            connection.isBlacklisted(arguments);
        } else if (command.equals("addBlacklisted")) {
            connection.addBlacklisted(arguments);
        } else if (command.equals("removeBlacklisted")) {
            connection.removeBlacklisted(arguments);
        } else if (command.equals("lsDir")) {
            String[] parts = arguments.split(" ");
            connection.lsDir(parts[0], toBoolean(parts[1]));
        } else if (command.equals("isAdmin")) {
            if (connection.isAdmin()) {
                System.out.println("User is admin!");
            } else {
                System.out.println("User is not admin!");
            }
        } else if (command.equals("isLoggedIn")) {
            if (connection.isLoggedIn()) {
                System.out.println("User is logged in!");
            } else {
                System.out.println("User is not logged in!");
            }
        } else if(command.equals("help")){
            connection.help();
        } else {
            return false;
        }
        return true;
    }

    private static boolean toBoolean(String string) {
        if (string.equals("true")) return true;
        return false;
    }

}
