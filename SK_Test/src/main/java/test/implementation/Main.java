package test.implementation;

import api.documentation.Connection;
import api.documentation.Driver;
import api.documentation.Manager;
//import local.implemetation.ManagerImplementation;

import java.io.File;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String type = sc.nextLine();
        //Driver driver = new Driver(args[0]);
        Manager manager = null;//TODO
        if(type.equals("local")){
            manager = local.implemetation.ManagerImplementation.getInstance();
            System.out.println("Uspesno konektovanje majmuni");
        }else if(type.equals("remote")){
            manager = remote.implementation.ManagerImplementation.getInstance();
        }
        //Manager manager = driver.getManager();
        Connection connection = manager.connect(type);
        connection.mkDir(File.separator,"root");
    }

}
