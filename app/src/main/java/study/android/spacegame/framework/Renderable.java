package study.android.spacegame.framework;


import android.graphics.Canvas;

/**
 * То, что мы можем нарисовать
 */
public interface Renderable{
    /**
     * Рисует объект на данной канве.
     */
    void render(Canvas canvas);

    /*
 * Задает порядок объектов.
 * Чем значение больше, тем объект дальше от наблюдателя
 */

        float zOrder();
    
}
