package net.zamasoft.font.cff;

import java.io.IOException;
import java.io.OutputStream;

class Type2Stack {
	private int size = 0;
	private final int[] values;

	public Type2Stack() {
		int size = 48;
		this.values = new int[size];
	}

	public void push(int value) {
		this.values[this.size] = value;
		++this.size;
	}

	public int get(int ix) {
		assert ix < this.size;
		return this.values[ix];
	}

	public int pop() {
		return this.values[--this.size];
	}

	public void clear() {
		this.size = 0;
	}

	public void clear(int count) {
		this.size -= count;
		System.arraycopy(this.values, count, this.values, 0, this.size);
	}

	public int size() {
		return this.size;
	}

	public void writeTo(OutputStream out, int op, int size) throws IOException {
		for (int i = this.size - size; i < this.size; ++i) {
			int a = this.values[i];
			if (a >= -107 && a <= 107) {
				out.write(a + 139);
			} else if (a >= 108 && a <= 1131) {
				a -= 108;
				out.write((a >> 8) + 247);
				out.write(a);
			} else if (a >= -1131 && a <= -108) {
				a += 108;
				out.write((-a >> 8) + 251);
				out.write(-a);
			} else if (a >= -32768 && a <= 32767) {
				out.write(28);
				out.write(a >> 8);
				out.write(a);
			} else {
				out.write(255);
				out.write(a >> 24);
				out.write(a >> 16);
				out.write(a >> 8);
				out.write(a);
			}
		}
		if ((op & 0xFF00) == 0x0C00) {
			out.write(op >> 8);
			out.write(op);
		} else {
			out.write(op);
		}
	}
}
