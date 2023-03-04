package org.lionheart.easyinvestor.type;

import lombok.Getter;
import lombok.Setter;
import org.lionheart.easyinvestor.exception.DataTypeException;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 物理量。
 */
public abstract class PhysicalQuantity<Unit extends Enum<Unit>>
        implements Serializable, EnumParser<Unit>, Comparable<PhysicalQuantity<Unit>> {
    private static final Pattern PATTERN = Pattern.compile("([0-9]+)\\s*([A-Za-z]+)");

    /**
     * 数值。
     */
    @Getter
    @Setter
    protected Number value;

    /**
     * 单位。
     */
    @Getter
    @Setter
    protected Unit unit;

    public void init(PhysicalQuantity<Unit> pq) {
        if (pq != null) {
            value = pq.getValue();
            unit = pq.getUnit();
        } else {
            throw new DataTypeException(
                    "Cannot create PhysicalQuantity instance with a null physical quantity reference!");
        }
    }

    public void init(String pqStr) {
        Matcher matcher = PATTERN.matcher(pqStr);
        boolean isMatched = matcher.find();
        if (isMatched) {
            String valueField = matcher.group(1);
            String unitField = matcher.group(2);
            this.value = Double.parseDouble(valueField);
            this.unit = parseUnit(unitField);
        } else {
            throw new DataTypeException("Invalid physical quantity format: " + pqStr);
        }
    }

    public Number getValueWithUnit(Unit dstUnit) {
        return switchUnit(value, unit, dstUnit);
    }

    public void switchUnit(String unit) {
        Unit dstUnit = parseUnit(unit);
        switchUnit(dstUnit);
    }

    public void switchUnit(Unit dstUnit) {
        if (unit.equals(dstUnit)) {
            return;
        }
        value = switchUnit(value, unit, dstUnit);
        unit = dstUnit;
    }

    public synchronized void switchUnitSync(Unit dstUnit) {
        if (unit.equals(dstUnit)) {
            return;
        }
        value = switchUnit(value, unit, dstUnit);
        unit = dstUnit;
    }

    public boolean isBiggerThan(PhysicalQuantity<Unit> anotherPq) {
        return compareTo(anotherPq) > 0;
    }

    public boolean isSmallerThan(PhysicalQuantity<Unit> anotherPq) {
        return compareTo(anotherPq) < 0;
    }

    public boolean isNoBiggerThan(PhysicalQuantity<Unit> anotherPq) {
        return compareTo(anotherPq) <= 0;
    }

    public boolean isNoSmallerThan(PhysicalQuantity<Unit> anotherPq) {
        return compareTo(anotherPq) >= 0;
    }

    @Override
    public int compareTo(PhysicalQuantity<Unit> anotherPq) {
        if (anotherPq == null) {
            throw new DataTypeException("Cannot compare with null physical quantity object!");
        }
        int unitCompareResult = compareUnit(anotherPq);
        if (unitCompareResult == 0) {
            return compareValue(value, anotherPq.getValue());
        }

        // 单位不同，先对齐单位。
        Number value1 = value;
        Number value2 = anotherPq.getValue();
        if (unitCompareResult > 0) {
            value1 = switchUnit(value1, unit, anotherPq.getUnit());
        } else {
            value2 = switchUnit(value2, anotherPq.getUnit(), unit);
        }

        return compareValue(value1, value2);
    }

    @Override
    public String toString() {
        return value + " " + unit.name().toLowerCase();
    }

    /**
     * 解析量纲单位。
     * @param unitStr 输入的量纲单位字符串。
     * @return 对应的量纲枚举值。
     */
    public abstract Unit parseUnit(String unitStr);

    @Override
    public Enum<Unit> parse(String typeStr) {
        return parseUnit(typeStr);
    }

    public int compareUnit(PhysicalQuantity<Unit> anotherPq) {
        return compareUnit(unit, anotherPq.getUnit());
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhysicalQuantity<Unit> pq = (PhysicalQuantity<Unit>) o;
        return compareTo(pq) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit, value);
    }

    /**
     * 将时间值从源单位切换到目标单位。
     */
    public abstract Number switchUnit(Number value, Unit srcUnit, Unit dstUnit);

    public void switchUnit(List<PhysicalQuantity<Unit>> pqList, Unit dstUnit) {
        if (pqList == null) {
            return;
        }
        for (PhysicalQuantity<Unit> pq : pqList) {
            pq.switchUnit(dstUnit);
        }
    }

    /**
     * 比较时间单位。
     * @return 单位1更大，返回1；单位2更大返回-1；相等返回0。
     */
    public int compareUnit(
            Unit unit1, Unit unit2) {
        return unit1.compareTo(unit2);
    }

    private static int compareValue(Number value1, Number value2) {
        float valueCompareResult = value1.floatValue() - value2.floatValue();
        if (valueCompareResult == 0F) {
            return 0;
        }
        return valueCompareResult > 0 ? 1 : -1;
    }
}
