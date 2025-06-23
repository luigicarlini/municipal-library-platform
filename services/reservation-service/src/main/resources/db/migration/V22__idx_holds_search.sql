CREATE INDEX IF NOT EXISTS idx_holds_search
ON holds (pickup_branch, status);
