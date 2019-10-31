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
        System.out.println("Input username and password divided with space (ex. admin 123)");
        un_pass = sc.nextLine().split(" ");
        Connection connection = manager.connect(path, un_pass[0], un_pass[1]);
        System.out.println("You are ready to go! If you need help just type \"help\" command to display all commands\n" +
                "Or \"help\" <command> to see what that command does.\nType \"exit\" to close the program");
        while (true) {
            String command;
            String arguments;
            String[] line;
            line = sc.nextLine().split("\\s+", 2);
            command = line[0];
            arguments = line[1];
            if (!doCommand(connection, command, arguments)) {
                System.out.println("Unknown command!");
            }
            if (line.equals("exit")) {
                break;
            }
        }
    }

    private static boolean doCommand(Connection connection, String command, String arguments) {
        if (command.equals("upload")) {
            if(!connection.isAdmin()){
                System.out.println("This command is only for admin!");
            }
            connection.upload(arguments);
        } else if (command.equals("download")) {
            connection.download(arguments);
        } else if (command.equals("set_meta")) {
            String[] parts = arguments.split(" ");
            connection.set_meta(parts[0], parts[1], parts[2]);
        } else if (command.equals("addUser")) {
            if(!connection.isAdmin()){
                System.out.println("This command is only for admin!");
            }
            String[] parts = arguments.split(" ");
            UserPrivilege userPrivilege = null;
            if (parts[2].equals("ADMIN")) {
                userPrivilege = UserPrivilege.ADMIN;
            } else if (parts[2].equals("GUEST")) {
                userPrivilege = UserPrivilege.GUEST;
            }
            connection.addUser(parts[0], parts[1], userPrivilege);
        } else if (command.equals("mkDir")) {
            if(!connection.isAdmin()){
                System.out.println("This command is only for admin!");
            }
            String[] parts = arguments.split(" ");
            connection.mkDir(parts[0], parts[1]);
        } else if (command.equals("mkFile")) {
            if(!connection.isAdmin()){
                System.out.println("This command is only for admin!");
            }
            String[] parts = arguments.split(" ");
            connection.mkFile(parts[0], parts[1]);
        } else if (command.equals("deleteItem")) {
            if(!connection.isAdmin()){
                System.out.println("This command is only for admin!");
            }
            String[] parts = arguments.split(" ");
            connection.deleteItem(parts[0], parts[1]);
        } else if (command.equals("isBlacklisted")) {
            connection.isBlacklisted(arguments);
        } else if (command.equals("addBlacklisted")) {
            connection.addBlacklisted(arguments);
        } else if (command.equals("removeBlacklisted")) {
            connection.removeBlacklisted(arguments);
        } else if (command.equals("lsDir")) {
            connection.lsDir(arguments);
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
        } else {
            return false;
        }
        return true;
    }

}
