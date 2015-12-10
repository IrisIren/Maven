package ecg;

class CircularBuffer {
    private Byte data[];
    private int head;
    private int tail;

    public CircularBuffer(Integer number) {
        data = new Byte[number];
        head = 0;
        tail = 0;
    }

    public boolean store(Byte value) {
        if (!bufferFull()) {
            data[tail++] = value;
            if (tail == data.length) {
                tail = 0;
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean isEmpty() {
        return head == tail;
    }

    public Byte read() {
        if (!isEmpty()) {
            Byte value = data[head++];
            if (head == data.length) {
                head = 0;
            }
            return value;
        } else {
            return null;
        }
    }

    private boolean bufferFull() {
        if (tail + 1 == head) {
            return true;
        }
        if (tail == (data.length - 1) && head == 0) {
            return true;
        }
        return false;
    }
}
