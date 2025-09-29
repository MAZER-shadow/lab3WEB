package se.ifmo.lab3web.point;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

@NoArgsConstructor
@Getter
@Setter
@Named("rBean")
@SessionScoped
public class RBean implements Serializable {
    private BigDecimal r = BigDecimal.valueOf(1);

    public void validateRBeanValue(FacesContext context, UIComponent component, Object value) {
        try {
            BigDecimal rValue = new BigDecimal(value.toString());
            verifyR(rValue);
        } catch (Exception e) {
            FacesMessage message = new FacesMessage("R должен быть выбран от 1 до 4 с шагом 0.25");
            throw new ValidatorException(message);
        }
    }

    private void verifyR(BigDecimal r) {
        final BigDecimal LOWER_BOUND = new BigDecimal("1");
        final BigDecimal UPPER_BOUND = new BigDecimal("4");
        final BigDecimal STEP = new BigDecimal("0.25");
        final BigDecimal ZERO = BigDecimal.ZERO;

        if (!(r.compareTo(LOWER_BOUND) >= 0 && r.compareTo(UPPER_BOUND) <= 0)) {
            throw new RuntimeException();
        }
        BigDecimal difference = r.subtract(LOWER_BOUND);

        BigDecimal remainder = difference.remainder(STEP).setScale(10, RoundingMode.HALF_UP);

        if (remainder.compareTo(ZERO) != 0) {
            throw new RuntimeException();
        }
    }

}
