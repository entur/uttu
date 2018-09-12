package no.entur.uttu.util;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalTime;

public class ValidationHelperTest {


    @Test
    public void isNotAfter_whenEitherTimeIsNull_shouldBeTrue() {
        Assert.assertTrue(ValidationHelper.isNotAfter(null, 1, LocalTime.MIDNIGHT, 0));
        Assert.assertTrue(ValidationHelper.isNotAfter(LocalTime.MIDNIGHT, 1, null, 0));
        Assert.assertTrue(ValidationHelper.isNotAfter(null, 1, null, 0));
    }


    @Test
    public void isNotAfter_whenOtherDayOffsetIsGreater_shouldBeTrue() {
        Assert.assertTrue(ValidationHelper.isNotAfter(LocalTime.MAX, 0, LocalTime.MIN, 1));
    }

    @Test
    public void isNotAfter_whenOtherLocalTimeIsGreater_shouldBeTrue() {
        Assert.assertTrue(ValidationHelper.isNotAfter(LocalTime.of(10, 0), 0, LocalTime.of(10, 1), 0));
    }

    @Test
    public void isNotAfter_whenEqual_shouldBeTrue() {
        Assert.assertTrue(ValidationHelper.isNotAfter(LocalTime.of(10, 0), 0, LocalTime.of(10, 0), 1));
    }

    @Test
    public void isNotAfter_whenOtherDayOffsetIsSmaller_shouldBeFalse() {
        Assert.assertFalse(ValidationHelper.isNotAfter(LocalTime.MIN, 1, LocalTime.MAX, 0));
    }

    @Test
    public void isNotAfter_whenOtherLocalTimeIsSmaller_shouldBeFalse() {
        Assert.assertFalse(ValidationHelper.isNotAfter(LocalTime.of(10, 1), 0, LocalTime.of(10, 0), 0));
    }
}
