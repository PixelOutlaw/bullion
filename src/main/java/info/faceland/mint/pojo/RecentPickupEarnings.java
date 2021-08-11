package info.faceland.mint.pojo;

public class RecentPickupEarnings {

  double amount;
  long timestamp;

  public RecentPickupEarnings(double amount) {
    this.amount = amount;
    timestamp = System.currentTimeMillis() + 1250;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
