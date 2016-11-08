package study.android.spacegame.framework;

/**
 * Простой класс двумерных векторов. Реализованы только более-менее нужные
 * операции над векторами.
 *
 */
public class Vector {
	public float x, y;

	public Vector(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Vector() {
	}

	public Vector set(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public Vector set(Vector to) {
		this.x = to.x;
		this.y = to.y;
		return this;
	}

	/**
	 * Добавляет к компонентам вектора данные значения
	 */
	public Vector add(float x, float y) {
		this.x += x;
		this.y += y;
		return this;
	}

	public Vector add(Vector what) {
		this.x += what.x;
		this.y += what.y;
		return this;
	}

	public Vector sub(Vector what) {
		this.x -= what.x;
		this.y -= what.y;
		return this;
	}

	public Vector sub(float x, float y) {
		this.x -= x;
		this.y -= y;
		return this;
	}

	/**
	 * Умножает вектор на величину
	 */
	public Vector scale(float scl) {
		this.x *= scl;
		this.y *= scl;
		return this;
	}

	/**
	 * Умножает вектор на вектор
	 */
	public Vector scale(float x, float y) {
		this.x *= x;
		this.y *= y;
		return this;
	}

	/**
	 * Вычисляет квадрат расстояния между точками
	 */
	public float dst2(Vector to) {
		return (float) (Math.pow(to.x - x, 2) + Math.pow(to.y - y, 2));
	}

	/**
	 * Нормализует вектор
	 */
	public Vector norm() {
	    double d = Math.sqrt(dst2(new Vector(0, 0)));
	    x /= d;
	    y /= d;
		return this;
	}

	/**
	 * Интерполирует вектор между ним и вторым вектором.
	 */
	public Vector interpolate(Vector to, float progress) {
		x = x + (to.x - x) * progress;
		y = y + (to.y - y) * progress;

		Vector a = new Vector(3, 5);
		Vector b = new Vector(2, 1);
		System.out.println(a.add(b).scale(3).dst2(b));

		return this;
	}

	/**
	 * Просто для красоты переопределим toString
	 */
	@Override
	public String toString() {
		return "(" + x + ";" + y + ")";
	}

}
