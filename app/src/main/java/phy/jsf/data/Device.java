package phy.jsf.data;

public class Device {
    public String id;
    public String name;
    public String addr_name;
    public int floor_num;
    public int room_num;

    public Device(String id, String name, String addr_name, int floor_num, int room_num) {
        this.id = id;
        this.name = name;
        this.addr_name = addr_name;
        this.floor_num = floor_num;
        this.room_num = room_num;
    }

    public Device() {
    }
}
