package study.android.spacegame.framework;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * В этом классе хранятся все данные игры. Не важно, какой, можно сделать свою,
 * так что можно считать это небольшим фреймворком.
 */
public class GameView extends View {

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GameView(Context context) {
		super(context);
	}

	/**
	 * Тут мы храним объекты. Да, мы не используем отдельного типа объектов -
	 * вместо этого наши объекты могут реализовывать интерфейсы Touchable,
	 * Renderable и Updatable, чтобы получать соответствующие функции. И это
	 * позволяет добавлять в обновление вещи, которые не надо рисовать (вроде
	 * циклов, которые постоянно применяют к объектам гравитацию), вещи, которые
	 * надо рисовать, но не надо обновлять, вроде фонов, и прочие подобные
	 * штуки.
	 * <p/>
	 * И ещё это делает общую механику объектов расширяемее - то есть для
	 * добавления какого-нибудь onDispose, вызываемого при очистке памяти, нам
	 * не придётся его реализовывать его в каждом объекте - нам просто
	 * понадобится подключить интерфейс Disposable туда, куда нужно, и выполнять
	 * только там где нужно. Очень удобно!
	 */
	private ArrayList<Object> objects = new ArrayList<Object>();

	/**
	 * Список объектов для добавления. Будем считать, что мы не будем добавлять
	 * больше 10 объектов за обновление, а даже если и будем - список
	 * расширится.
	 **/
	ArrayList<Object> objectAddBuffer = new ArrayList<Object>(10);

	/**
	 * Добавляет объект в список для обновления
	 */
	public void addObject(Object object) {
		objectAddBuffer.add(object);
	}

	/**
	 * Удаляет объект из списка
	 */
	public void removeObject(Object object) {
		objects.remove(object);
		
	}

	/**
	 * Удаляет все игровые объекты
	 */
	public void clear() {
		objects.clear();
	}

	// только для тестирования
	float tmpX = 0;
	float tmpY = 100;
	Paint tmpPaint = new Paint();

	/**
	 * Рисует все наши объекты на канву.
	 */
	public void render(Canvas canvas) {
		// canvas.drawCircle(tmpX, tmpY, 20, tmpPaint);
		for (int i = 0; i < objects.size(); i++) {
			final Object o = objects.get(i);

			/*
			 * Проверяем, может ли объект быть нарисован. И рисуем, если может.
			 */

			if (o instanceof Renderable)
				((Renderable) o).render(canvas);

		}
	}

	private final ZComparator ZCOMP = new ZComparator();
	
	/**
	 * Выполняет цикл обновления. deltaTime - это количество времени, прошедшее
	 * с предыдущего обновления в секундах.
	 **/
	public void update(float deltaTime) {

		// tmpX += deltaTime * 10;
		for (int i = objects.size() - 1; i >= 0 && i < objects.size(); i--) {
			final Object o = objects.get(i);

			if (o instanceof Updatable)
				((Updatable) o).update(deltaTime);
		}

		// Проверяем наш буфер добавленных объектов
		if (!objectAddBuffer.isEmpty()) {

			objects.addAll(objectAddBuffer);
			Collections.sort(objects, ZCOMP);
			objectAddBuffer.clear();
		}

	}

	public float timeScale = 1;
	long lastUpdate = -1;

	/**
	 * Вызываем прорисовку и обновление.
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// Просто обновляем все объекты. Для удобства переводим миллисекунды в
		// секунды.
		// Дополнительно умножаем на скорость времени
		long currentTime = System.currentTimeMillis();
		update((currentTime - lastUpdate) * 0.001f * timeScale);
		// Потом рисуем
		render(canvas);
		// А потом говорим системе, что мы хотим перерисоваться и в следующий
		// раз.
		invalidate();
		// И выставляем новое время предудушего запуска
		lastUpdate = currentTime;
	}
	
	@Override
	protected void onAttachedToWindow() { 
		super.onAttachedToWindow();
		final long currentTime = System.currentTimeMillis();
		/*
		 * Тут небольшая проверка на размер deltaTime - поскольку при большом
		 * значении deltaTime будут происходить плохие вещи с объектами. Так что
		 * если разница currentTime и lastUpdate превышает секунду - то это
		 * значит, что со времени последнего обновления прошло больше секунды, и
		 * тогда сводим deltaTime до 12ms. Такое случится при первом запуске и
		 * при возвращении к Activity с этим View.
		 */
		if (currentTime - lastUpdate > 1000)
			lastUpdate = currentTime - 12;
	}

	
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// touch(event);
		boolean wasTouched = false;
		boolean justTouch = (event.getAction() == MotionEvent.ACTION_DOWN);
		float touchX = event.getX();
		float touchY = event.getY();
		// в обратном порядке - ближние объекты получают событие первыми
		// и передача прекращается, как только какой-то объект возвратит true
		for (int i = objects.size() - 1 ; i >= 0 && !wasTouched; i--) {

			if (objects.get(i) instanceof Touchable) {
				wasTouched = ((Touchable) objects.get(i)).
	                            onScreenTouch(touchX, touchY, justTouch);
			}
		}
		return false;
	} 


}
