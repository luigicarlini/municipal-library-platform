-- V23__idx_holds_status_branch.sql
CREATE INDEX IF NOT EXISTS idx_holds_live_branch
  ON holds (pickup_branch)
 WHERE status = 'PLACED';
