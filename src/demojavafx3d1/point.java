package demojavafx3d1;

public class point implements Comparable<point>
{
    private double x, y, z, normal_x, normal_y, normal_z;
    private int color = -1;
    private int[] rgb = null;
    private double[] properties = null;
    private DataType type;
    private int mergedCounter = 0;
    final int MAX_COUNT = 5;

    public point(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = DataType.XYZ;
    }

    public point(double x, double y, double z, int color)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
        this.type = DataType.XYZRGB;
    }

    public point(double x, double y, double z, double normal_x, double normal_y, double normal_z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.normal_x = normal_x;
        this.normal_y = normal_y;
        this.normal_z = normal_z;
        this.type = DataType.XYZNORMAL;
    }

    public boolean counterInc()
    {
        if (mergedCounter > MAX_COUNT)
            return false;
        else{
            mergedCounter ++;
            return true;
        }
    }

    public DataType getType()
    {
        return this.type;
    }

    public double getX()
    {
        return this.x;
    }

    public double getY()
    {
        return this.y;
    }

    public double getZ()
    {
        return this.z;
    }

    public int getRGB()
    {
        return this.color;
    }

    public int[] parseRGB()
    {
        if (rgb == null && color != -1)
        {
            rgb = new int[3];
            rgb[0] = (color >> 16) & 0x0000ff;
            rgb[1] = (color >> 8)  & 0x0000ff;
            rgb[2] = (color) & 0x0000ff;
        }

        return rgb;
    }

    public double[] getProperties()
    {
        switch (type)
        {
            case XYZ:
                return getXYZProperties();
            case XYZRGB:
                return getXYZRGBProperties();
            case XYZNORMAL:
                return getXYZNORMALProperties();
            default:
                return null;
        }
    }

    private double[] getXYZProperties()
    {
        if (properties == null)
            properties = new double[] {this.x, this.y, this.z};
        return properties;
    }

    private double[] getXYZRGBProperties()
    {
        if (properties == null)
            properties = new double[] {this.x, this.y, this.z, this.color};
        return properties;
    }

    private double[] getXYZNORMALProperties()
    {
        if (properties == null)
            properties = new double[] {this.x, this.y, this.z, this.normal_x, this.normal_y, this.normal_z};
        return properties;
    }
    @Override
    public int compareTo(point other)
    {
        if (other == null)
            return 1;
        else if (this.x > other.getX())
            return 1;
        else if (this.x < other.getX())
            return -1;
        else if (this.y > other.getY())
            return 1;
        else if (this.y < other.getY())
            return -1;
        else if (this.z > other.getZ())
            return 1;
        else if (this.z < other.getZ())
            return -1;
        else
            return 0;
    }

    public double disTo(point other)
    {
        if (other == null)
            return 0;
        else
            return Math.sqrt((this.x - other.getX()) * (this.x - other.getX()) + (this.y - other.getY()) * (this.y - other.getY()) + (this.z - other.getZ()) * (this.z - other.getZ()));
    }
}


