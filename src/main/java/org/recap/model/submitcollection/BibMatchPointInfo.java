package org.recap.model.submitcollection;

import com.google.common.base.Objects;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * Created by rajeshbabuk on 17/Sep/2021
 */
@Data
public class BibMatchPointInfo {
    private String title;
    private String lccn;
    private List<String> isbn;
    private List<String> issn;
    private List<String> oclc;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BibMatchPointInfo that = (BibMatchPointInfo) o;
        return Objects.equal(title, that.title) &&
                Objects.equal(lccn, that.lccn) &&
                ((isbn == null && that.isbn == null) || (isbn != null && that.isbn != null && CollectionUtils.isEqualCollection(isbn, that.isbn))) &&
                ((issn == null && that.issn == null) || (issn != null && that.issn != null && CollectionUtils.isEqualCollection(issn, that.issn))) &&
                ((oclc == null && that.oclc == null) || (oclc != null && that.oclc != null && CollectionUtils.isEqualCollection(oclc, that.oclc)));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(title, lccn, isbn, issn, oclc);
    }
}
