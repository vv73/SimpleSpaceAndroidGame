package study.android.spacegame.framework;

/**
 * То, что может реагировать на нажатия
 *
 */
public interface Touchable extends Updatable {
    /**
     * Вызывается при нажатии на экран.
     *
     * @param justTouched true, если пользователь начал нажимать на экран только что.
     * @param touchX X-координата места нажатия на экран
     * @param touchY Y-координата места нажатия на экран
     * @return true, если нажатие было обработано объектом.
     */
    boolean onScreenTouch(float touchX, float touchY, boolean justTouched);
}
