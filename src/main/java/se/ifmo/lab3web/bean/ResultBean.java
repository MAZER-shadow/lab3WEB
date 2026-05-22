package se.ifmo.lab3web.bean;


import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.ifmo.lab3web.dto.HitDTO;
import se.ifmo.lab3web.entity.Hit;
import se.ifmo.lab3web.error.ErrorView;
import se.ifmo.lab3web.point.RBean;
import se.ifmo.lab3web.point.XBean;
import se.ifmo.lab3web.point.YBean;
import se.ifmo.lab3web.service.HitService;
import se.ifmo.lab3web.util.Messages;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Setter
@Getter
@Named("resultBean")
@SessionScoped
@NoArgsConstructor
public class ResultBean implements Serializable {
    private static final Logger LOG = Logger.getLogger(ResultBean.class.getName());
    public static final String ERROR_MESSAGE = Messages.get("error.unexpected");
    @Inject
    private ErrorView errorView;
    @Inject
    private RBean rBean;
    @Inject
    private XBean xBean;
    @Inject
    private YBean yBean;
    @Inject
    private HitService hitService;
    private HttpSession session;

    private LinkedList<Hit> results = new LinkedList<>();

    @PostConstruct
    public void init() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            session = (HttpSession) context.getExternalContext().getSession(true);
            String userId = session.getId();
            List<Hit> resultsEntities = hitService.findAllByUserId(userId);
            results = new LinkedList<>(resultsEntities);
        } catch (Throwable e) {
            LOG.log(Level.SEVERE, "Error in ResultBean.init()", e);
            errorView.showError(ERROR_MESSAGE);
        }
    }

    public void addResult() {
        try {
            BigDecimal x = xBean.getX();
            BigDecimal y = yBean.getY();
            BigDecimal r = rBean.getR();
            HitDTO hitDTO = HitDTO.builder()
                    .x(x)
                    .y(y)
                    .r(r)
                    .build();
            Hit hit = hitService.createHit(hitDTO, session);
            results.addFirst(hit);
        } catch (Throwable e) {
            LOG.log(Level.SEVERE, "Error in ResultBean.addResult()", e);
            errorView.showError(ERROR_MESSAGE);
        }
    }

    public void deleteAllByUserId(HttpSession session) {
        try {
            int valueOfDelete = hitService.deleteAllByUserId(session);
            results.clear();
            xBean.setX(null);
            yBean.setY(null);
            rBean.setR(new BigDecimal(1));
        } catch (Throwable e) {
            LOG.log(Level.SEVERE, "Error in ResultBean.deleteAllByUserId()", e);
            errorView.showError(ERROR_MESSAGE);
        }
    }
}
