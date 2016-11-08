package study.android.spacegame.framework;

import java.util.Comparator;

//Cравниватель ZOrder-а. Если оба объекта имеют z-order, то сравниваем значения,
// иначе приоритетным считается объект с имеющимся zorder-ом.
class ZComparator implements Comparator<Object>{
    @Override
    public int compare(Object lhs, Object rhs) {
        if (lhs instanceof Renderable && rhs instanceof Renderable)
            return ((Renderable) lhs).zOrder() - ((Renderable) rhs).zOrder() > 0 ? 1 : -1;
        if (lhs instanceof Renderable)
            return 1;
        if (rhs instanceof Renderable)
            return -1;
        return 0;
    }
};