package com.faas.server_manager;

import com.faas.common.TaggedConnection;

import java.net.Socket;

public class WorkerStats {
    private int id;
    private int occupiedMemory;
    private int totalMemory;
    private TaggedConnection conn;

    public WorkerStats(int id, int totalMemory, TaggedConnection conn) {
        this.id = id;
        this.occupiedMemory = 0;
        this.totalMemory = totalMemory;
        this.conn = conn;
    }

    public int getId() {
        return id;
    }

    public int getOccupiedMemory() {
        return occupiedMemory;
    }

    public int getTotalMemory() {
        return totalMemory;
    }

    public void update(int memory, boolean subtract) {
        if (subtract) {
            occupiedMemory -= memory;
        } else {
            occupiedMemory += memory;
        }
    }
}
