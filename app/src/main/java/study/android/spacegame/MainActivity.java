package study.android.spacegame;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import study.android.spacegame.framework.GameView;
import study.android.spacegame.framework.Renderable;
import study.android.spacegame.framework.Touchable;
import study.android.spacegame.framework.Updatable;
import study.android.spacegame.framework.Vector;

public class MainActivity extends Activity implements Updatable {

	/*
	 * Общие переменные
	 */

	// View с игрой
	private GameView game;
	// Текущий счет
	private int score = 0;
	// View со счетом
	private TextView scoreScreen;
	// Картинки с астероидами
	Bitmap asteroid;
	// Картинки с частицами взрывов
	Bitmap explosion1;
	Bitmap explosion2;
	// Счетчик жизней
	LinearLayout lives;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Достанем нашу view с игрой
		game = (GameView) (findViewById(R.id.game));
		// Загрузим картинку с астероидом
		asteroid = BitmapFactory.decodeResource(getResources(), R.drawable.asteroid1);
		// И частицами
		explosion1 = BitmapFactory.decodeResource(getResources(), R.drawable.explosion1);
		explosion2 = BitmapFactory.decodeResource(getResources(), R.drawable.explosion2);
		// Достанем окошко со счетом
		scoreScreen = (TextView) findViewById(R.id.score);
		// И счетчик жизней
		lives = (LinearLayout) findViewById(R.id.lives);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// добавляем себя в игру
		game.addObject(this);
		game.addObject(new Stars(game));
	}

	// прошедшее время с последнего добавления
	float timeElapsed = 0;

	@Override
	public void update(float deltaTime) {
		// Каждую секунду добавляем объект
		if (timeElapsed > 0.5) {
			/*
			 * game.addObject(new
			 * Asteroid(BitmapFactory.decodeResource(this.getResources(),
			 * R.drawable.asteroid1), game));
			 */
			game.addObject(new Asteroid(asteroid, game));
			// обновились только что
			timeElapsed = 0;
		} else {
			// игровая секунда еще не прошла - ждем
			timeElapsed += deltaTime;

		}
	}

	public void pause(View v) {
		if (game.timeScale == 0) {
			unpause();
			return;
		}
		// Останавливаем время
		game.timeScale = 0;
		// И ещё добавляем перехват всех нажатий.
		game.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// Как только произошло нажатие, выключаем паузу
				unpause();
				return true;
			}
		});
	}

	public void unpause() {
		// Возвращаем время и убираем перехват нажатий.
		game.timeScale = 1;
		game.setOnTouchListener(null);
	}

	public void damage() {
		if (lives.getChildCount() > 0) {
			lives.removeViewAt(0);
			return;
		}
		pause(null);

		// Показываем окошко GameOver-ом
		// Создаем окно
		new AlertDialog.Builder(this)
				// Устанавливаем для него основное сообщение
				.setMessage("Game over! Ваш счет: " + score)
				// Делаем обязательным нажатие на кнопку,
				// иначе можно нажать на свободную область, и оно скроется
				.setCancelable(false)
				// Добавляем кнопку согласия (можно добавить и кнопку отказа)
				// Функция принимает обработчик, реализующий интерфейс
				// OnClickListener
				.setPositiveButton("Начать заново", new OnClickListener() {
					// что делать, когда пользователь нажмет на кнопку
					@Override
					public void onClick(DialogInterface dialog, int which) {
						restart();
					}
				})
				// показываем окно
				.show();

	}



	public void restart() {
		// Обнуляем очки
		score = 0;
		scoreScreen.setText("0");
		// Очищаем экран и добавляем объекты для игры
		game.clear();
		game.addObject(new Stars(game));
		game.addObject(this);
		// Выставляем три жизни
		while (lives.getChildCount() < 3) {
			ImageView life = new ImageView(this);
			life.setImageResource(R.drawable.ship);
			lives.addView(life);
		}
		// Снимаем игру с паузы
		unpause();
	}

	/**
	 * Это наши астероиды.
	 */
	class Asteroid implements Updatable, Renderable, Touchable {

		@Override
		public float zOrder() {
			// Положение астероида у нас зависит от его размера
			return diameter;
		}

		GameView game;

		// Текущая координата
		Vector coord;
		Vector velocity;

		// Поворот в градусах
		float spin = 0;
		// Скорость поворота
		float spin_speed = (float) (Math.random() * 100 - 50);
		// Размер астероида в пикселях
		float diameter = 1;
		// Нужная картинка с астероидом
		Bitmap image;

		public Asteroid(Bitmap image, GameView game) {

			this.game = game;
			this.image = image;

			Vector center = new Vector((float) game.getWidth() / 2, game.getHeight() / 2);

			// Генерируем случайный вектор начала движения
			coord = new Vector((float) (Math.random()), (float) (Math.random())).scale(game.getWidth(),
					game.getHeight());

			// Генерируем вектор скорости (разлет от центра экрана)
			velocity = new Vector(coord.x, coord.y).sub(center)
					// Нормализуем его
					.norm();
		}

		// Старайтесь создавать как можно меньше объектов в часто выполняемых
		// операциях, таких как render. Это быстро заполняет память.
		RectF dst = new RectF();

		Paint paint = new Paint();

		@Override
		public void render(Canvas canvas) {
			// Выставляем размер астероида в нужный нам
			dst.set(0, 0, diameter, diameter);
			dst.offset((int) coord.x, (int) coord.y);
			// И ещё немного сдвигаем, чтобы координата астероида
			// указывала на его центр
			dst.offset((int) (-diameter / 2), (int) (-diameter / 2));

			// Теперь повернём наш астероид.
			// Для этого надо:
			// Cохранить текущее состояние канвы
			canvas.save();
			// Повернуть канву на нужный нам угол
			canvas.rotate(spin, coord.x, coord.y);
			// Нарисовать астероид
			canvas.drawBitmap(image, null, dst, paint);
			// И вернуть канву в начальное положение.
			canvas.restore();

			// Мы повернули Вселенную вокруг астероида, чтобы повернуть его :)
		}

		@Override
		public boolean onScreenTouch(float touchX, float touchY, boolean justTouched) {
			// просто удаляем его, если по нему нажали только что,
			// Для определения расположения используем dst - квадрат,
			// в который был нарисован астероид в последнем обновлении.
			if (justTouched && dst.contains(touchX, touchY)) {
				game.removeObject(this);
				score++;
				scoreScreen.setText(score + "");
				// и добавляем взрыв
				/*
				 * Устаревший вариант: game.addObject(new
				 * Explosion(BitmapFactory.decodeResource(game.getResources(),
				 * R.drawable.explosion2), this, game));
				 */

				game.addObject(new Explosion(explosion2, this, game));
				return true;
			}
			return false;
		}

		Vector tmpVector = new Vector();

		@Override
		public void update(float deltaTime) {
			// Обновим значение поворота
			spin += deltaTime * spin_speed;

			// Изменяем положение астероида
			// чем ближе, тем быстрее
			velocity = velocity.scale((float) (1 + 0.05 * deltaTime));
			// Мы будем увеличивать диаметр астероида, как будто он приближается
			diameter += (float) (Math.sqrt(velocity.dst2(new Vector(0, 0)))) * deltaTime * 20;
			coord.add(tmpVector.set(velocity).scale((float) (deltaTime * 20)));

			// Если размер астероида больше половины высоты экрана - удаляем
			// астероид

			if (diameter > game.getHeight() / 2) {
				game.removeObject(this);
				// Ecли он еще виден на экране, то будем
				// считать, что произошло столкновение - удаляем астероид
				// и добавляем взрыв
				if (dst.intersects(0, 0, game.getWidth(), game.getHeight())) {
					/*
					 * Устаревший вариант: game.addObject(new Explosion(
					 * BitmapFactory.decodeResource(game.getResources(),
					 * R.drawable.explosion1), this, game));
					 */
					game.addObject(new Explosion(explosion1, this, game));
					damage();
				}

			}
		}

	}

	/**
	 * Всякие взрывы!
	 */
	public class Explosion implements Updatable, Renderable {

		GameView game;

		// Сколько у нас частиц во взрыве
		static final int PARTICLE_NUM = 10;
		// Картинка частицы
		private final Bitmap particle;
		// Текущий диаметр частицы
		private float diameter;
		// Начальный диаметр частицы
		private float start_diameter;
		// Скорости отдельных частиц.
		private ArrayList<Vector> speeds = new ArrayList<Vector>(PARTICLE_NUM);
		// Центр взрыва
		private Vector center;
		// zOrder взрыва
		private final float z;

		public Explosion(Bitmap particle, Asteroid from, GameView game) {

			this.game = game;

			this.particle = particle;

			// Копируем данные о положении взрыва из астероида
			center = new Vector().set(from.coord);
			start_diameter = diameter = from.diameter;
			z = from.zOrder();

			// Генерируем взрыв
			for (int i = 0; i < PARTICLE_NUM; i++) {
				final Vector vector = new Vector((float) Math.random(), (float) Math.random()).sub(0.5f, 0.5f).norm()
						.scale((float) Math.random());
				speeds.add(vector);
			}
		}

		Vector tmpVector = new Vector();
		private Rect dst = new Rect();
		Paint paint = new Paint();

		@Override
		public void render(Canvas canvas) {
			// Расстояние от эпицентра взрыва. Да, мы не используеми отдельных
			// переменных
			// для интерполяции, просто считаем по разнице начального диаметра и
			// текущего.
			// Так не приходится сохранять положение каждой частицы, и
			// контролировать размер
			// взрыва легче.
			float distance = (start_diameter - diameter) * 1.5f;
			// Тут всё довольно просто, и рисуется всё так же, как и в
			// астероиде.

			for (int i = 0; i < PARTICLE_NUM; i++) {
				Vector speed = speeds.get(i);
				// Единственное отличие - поворот вычисляется на основе вектора,
				// это простейшая тригонометрия.
				float rotation = (float) Math.toDegrees(Math.atan2(speed.y, speed.x));

				tmpVector.set(speed).scale(distance).add(center);
				dst.set(0, 0, (int) diameter, (int) diameter);
				dst.offset((int) tmpVector.x, (int) tmpVector.y);
				dst.offset((int) -diameter / 2, (int) -diameter / 2);

				paint.setColor(Color.WHITE);
				paint.setAlpha((int) (255 * (diameter / start_diameter)));
				canvas.save();
				canvas.rotate(rotation, tmpVector.x, tmpVector.y);
				canvas.drawBitmap(particle, null, dst, paint);
				canvas.restore();
			}
		}

		@Override
		public void update(float deltaTime) {
			// Просто уменьшаем диаметр, а после того,
			// как он становится меньше 1 - совершаем суицид.
			diameter *= 1 - deltaTime;
			if (diameter <= 1)
				game.removeObject(this);
		}

		// удаленность взрыва совпадает с удаленностью уничтоженного астероида
		@Override
		public float zOrder() {
			return z;
		}
	}

	/**
	 * Это будет наш звездный фон
	 */
	class Stars implements Renderable, Updatable {

		GameView game;

		// Количество звёзд
		static final int STAR_NUM = 200;
		// Тут у нас будут храниться координаты и "яркость" звёзд
		ArrayList<Vector> coords = new ArrayList<Vector>(STAR_NUM);
		ArrayList<Integer> alpha = new ArrayList<Integer>(STAR_NUM);

		public Stars(GameView game) {
			this.game = game;
			// Сейчас мы сгенерируем звезды
			for (int i = 0; i < STAR_NUM; i++) {
				coords.add(new Vector((float) (Math.random() * game.getWidth()),
						(float) (Math.random() * game.getHeight())));
				alpha.add((int) (Math.random() * 256));
			}
		}

		Paint paint = new Paint();

		@Override
		public void render(Canvas canvas) {
			// Рисуем черное небо
			canvas.drawColor(Color.BLACK);
			// Рисуем звёзды
			paint.setColor(Color.WHITE);
			for (int i = 0; i < coords.size(); i++) {
				final Vector coord = coords.get(i);
				paint.setAlpha(alpha.get(i));
				canvas.drawPoint(coord.x, coord.y, paint);
			}
		}

		@Override
		public void update(float deltaTime) {
			// Применяем изменение alpha на все звёзды
			for (int i = 0; i < coords.size(); i++) {
				// Достаём данные
				int a = alpha.get(i);
				a += (int) (Math.random() * 51 - 25);
				// Если выходим за рамки возможной прозрачности 0..255,
				// «возвращаем» новое значение в пределы.
				if (a > 255)
					a = 255;
				if (a < 0)
					a = 0;
				// Закладываем новые данные
				alpha.set(i, a);

			}
		}

		// Звезды рисуются подо всем.
		@Override
		public float zOrder() {
			return -1.0f;
		}
	}

	// конец класса MainActivity
}
