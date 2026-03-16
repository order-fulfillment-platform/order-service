CREATE TABLE orders (
	id			 UUID				NOT NULL,
	customer_id  UUID				NOT NULL,
	status		 VARCHAR(50)		NOT NULL,
	total_amount NUMERIC(10,2)		NOT NULL,
	created_at	 TIMESTAMP			NOT NULL,
	CONSTRAINT pk_orders PRIMARY KEY (id)
);

CREATE TABLE order_items (
	id			 UUID				NOT NULL,
	order_id	 UUID				NOT NULL,
	product_id	 UUID				NOT NULL,
	quantity	 INTEGER			NOT NULL,
	unit_price	 NUMERIC(10,2)		NOT NULL,
	CONSTRAINT pk_order_items PRIMARY KEY (id),
	CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
		REFERENCES orders(id)
		ON DELETE CASCADE
);

CREATE TABLE outbox_events (
	id 			 UUID				NOT NULL,
	aggregate_id UUID				NOT NULL,
	event_type	 VARCHAR(100)		NOT NULL,
	payload		 TEXT				NOT NULL,
	created_at	 TIMESTAMP			NOT NULL,
	processed	 BOOLEAN			NOT NULL DEFAULT FALSE,
	CONSTRAINT pk_outbox_events PRIMARY KEY (id)
);

CREATE INDEX idx_outbox_events_processed
	ON outbox_events(processed)
	WHERE processed = FALSE