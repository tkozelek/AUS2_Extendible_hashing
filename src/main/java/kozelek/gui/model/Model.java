package kozelek.gui.model;

import kozelek.db.Database;
import kozelek.objekty.Car;
import kozelek.objekty.Generator;
import kozelek.objekty.HashObjekty.CarId;
import kozelek.objekty.HashObjekty.CarSPZ;
import kozelek.objekty.Servis;

import java.time.LocalDate;
import java.util.ArrayList;

public class Model {
    private Database database;
    private Generator generator;

    public Model(String filename, int clusterHeap, int clusterId, int clusterSpz) {
        this.database = new Database(filename, clusterId, clusterSpz, clusterHeap);
        this.generator = new Generator();
    }

    public void generujAVloz(int pocet) {
        ArrayList<Car> cars = this.generator.generuj(pocet);
        for (Car car : cars) {
            this.database.insert(car);
        }
    }

    public void generujAVloz(int od, int pocet, int servisCount, int popisCount) {
        ArrayList<Car> cars = this.generator.generujIncrement(od, pocet, servisCount, popisCount);
        for (Car car : cars) {
            this.database.insert(car);
        }
    }

    public void insert(Car car) {
        database.insert(car);
    }

    public Car findCarById(int id) {
        return database.findCarById(id);
    }

    public Car findCarBySPZ(String spz) {
        return database.findCarBySPZ(spz);
    }

    public void insert(String meno, String priezvisko, int id, String spz) {
        database.insert(meno, priezvisko, id, spz);
    }

    public Car addServiceVisitById(int id, double cena, LocalDate datum, ArrayList<String> popis, int idServis) {
        return database.addServiceVisitById(id, cena, datum, popis, idServis);
    }

    public void addServiceVisitBySPZ(String SPZ, double cena, LocalDate datum, String[] popis, int id) {
        database.addServiceVisitBySPZ(SPZ, cena, datum, popis, id);
    }

    public void addServiceVisitById(int id, Servis servis) {
        database.addServiceVisitById(id, servis);
    }

    public void addServiceVisitBySPZ(String spz, Servis servis) {
        database.addServiceVisitBySPZ(spz, servis);
    }

    public void editCar(Car oldCar, Car newCar) {
        database.editCar(oldCar, newCar);
    }

    public void deleteServiceVisitById(int id, Servis servis) {
        database.deleteServiceVisitById(id, servis);
    }

    public void deleteServiceVisitBySPZ(String spz, Servis servis) {
        database.deleteServiceVisitBySPZ(spz, servis);
    }

    public void deleteCarById(int id) {
        database.deleteCarById(id);
    }

    public void deleteCarBySPZ(String spz) {
        database.deleteCarBySPZ(spz);
    }

    public ArrayList<Car> getAllCars() {
        return database.getAllCars();
    }

    public ArrayList<CarId> getAllCarIds() {
        return database.getAllCarsId();
    }

    public ArrayList<CarSPZ> getAllCarSPZs() {
        return database.getAllCarsSPZ();
    }

    public void saveStates() {
        database.saveStates();
    }

    public void close() {
        database.close();
    }

    public void deleteAllFiles() {
        database.zmazSubory();
    }

    public void printAllServises() {
        database.printAllServis();
    }

    public void printAllCarSPZ() {
        database.printAllSPZHashBlocks();
    }

    public void printAllCarId() {
        database.printAllIDHashBlocks();
    }

    public ArrayList<String> hbIdToString() {
        return database.stringDirectoryCarId();
    }

    public ArrayList<String> hbSpzToString() {
        return database.stringDirectoryCarSpz();
    }

    public ArrayList<String> allHeapBlocksToString() {
        return database.allHeapBlocksToString();
    }

    public String heapToString() {
        return database.printHeapFile();
    }

    public String hfCarIdToString() {
        return database.printHashFileCarId();
    }

    public String hfCarSpzToString() {
        return database.printHashFileCarSpz();
    }

    public void printAllHeapBlocks() {
        database.printAllHeapBlocks();
    }
}
