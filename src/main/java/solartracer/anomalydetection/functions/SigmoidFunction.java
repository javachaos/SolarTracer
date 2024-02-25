/*******************************************************************************
 * Copyright (c) 2022 Fred
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred - initial API and implementation
 ******************************************************************************/
package solartracer.anomalydetection.functions;

/**
 * Sigmoid activation function.
 */
public final class SigmoidFunction extends BaseFunction {

    public SigmoidFunction() {
        super(new Function(), FunctionType.SIGMOID);
    }

    @Override
    public double activate(double v) {
        return 1.0 / (1.0 + Math.exp(-v));
    }

    @Override
    public double derivative(final double v) {
        return activate(v) * (1.0 - activate(v));
    }

}
