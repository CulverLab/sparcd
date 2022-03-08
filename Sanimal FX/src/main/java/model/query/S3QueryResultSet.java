package model.query;

import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a result set from a query
 */
public class S3QueryResultSet
{
    // The list of rows
    List<S3QueryResultRow> rows = new ArrayList<S3QueryResultRow>();

    /**
     * Formats the instance to string
     */
    @Override
    public String toString()
    {
        int numRows = this.rows.size();

        StringBuilder sb = new StringBuilder();
        sb.append("S3QueryResultSet");
        sb.append("\n\t number of rows:");
        sb.append(this.rows.size());
        if (numRows <= 0)
            sb.append("\n\t no data");
        else
        {
            int idx = 0;
            while (idx < numRows && idx < 5)
            {
                S3QueryResultRow curRow = rows.get(idx);
                sb.append("\n\t Row: ");
                sb.append(idx + 1);
                sb.append("\n\t\t 0: '");
                sb.append(curRow.get(0));
                sb.append("'\t 1: '");
                sb.append(curRow.get(1));
                sb.append("'");

                idx++;
            }
            if (numRows >= 5)
            {
                sb.append("\n\t  addtional ");
                sb.append(numRows - 5 + 1);
                sb.append(" rows");
            }
        }

        return sb.toString();
    }

    /**
     * Adds a new row to the result set
     * 
     * @param path the first column value
     * @param name the second column value
     */
    public void addRow(final String path, final String name)
    {
        S3QueryResultRow newRow = S3QueryResultRow.instance(path, name);
        this.rows.add(newRow);
    }

    /**
     * Searches for a matching row
     * 
     * @param path the first column value
     * @param name the second column value
     * @return {@code boolean} whether a matching row was found
     */
    public boolean findRow(final String path, final String name)
    {
        for (S3QueryResultRow oneRow: this.rows)
        {
            if (oneRow.size() >= 2)
            {
                if (oneRow.get(0).equals(path) && oneRow.get(1).equals(name))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns the list of results
     * 
     * @return the list of results
     */
    public List<S3QueryResultRow> getResults()
    {
        return this.rows;
    }
}
